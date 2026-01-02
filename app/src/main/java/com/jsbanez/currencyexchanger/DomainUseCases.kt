package com.jsbanez.currencyexchanger

class GetRatesUseCase(private val repo: RatesRepository) {
    suspend operator fun invoke(): RatesDomain = repo.getRates()
}

class ConvertAmountUseCase {
    fun convert(amount: Double, from: String, to: String, rates: RatesDomain): Double? {
        if (from == to) return amount
        val base = rates.base
        val quotes = rates.quotes
        fun toBase(value: Double, code: String): Double? = when (code) {
            base -> value
            else -> quotes[code]?.let { value / it }
        }
        fun fromBase(value: Double, code: String): Double? = when (code) {
            base -> value
            else -> quotes[code]?.let { value * it }
        }
        val inBase = toBase(amount, from) ?: return null
        return fromBase(inBase, to)
    }
}

class PerformExchangeUseCase(
    private val balancesRepo: BalancesRepository,
    private val convert: ConvertAmountUseCase
) {
    fun canExchange(amount: Double, sell: String, buy: String): Boolean {
        if (amount <= 0) return false
        if (sell == buy) return false
        val bal = balancesRepo.getBalances().values[sell] ?: 0.0
        return amount <= bal
    }

    fun perform(amount: Double, sell: String, buy: String, rates: RatesDomain): Boolean {
        val received = convert.convert(amount, sell, buy, rates) ?: return false
        val current = balancesRepo.getBalances().values.toMutableMap()
        val newSell = (current[sell] ?: 0.0) - amount

        if (newSell < 0) return false
        current[sell] = maxOf(0.0, newSell)
        current[buy] = (current[buy] ?: 0.0) + received
        balancesRepo.update(BalancesDomain(current))
        return true
    }
}
