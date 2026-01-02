package com.jsbanez.currencyexchanger

class RatesRepositoryImpl(private val api: RatesApi) : RatesRepository {
    override suspend fun getRates(): RatesDomain {
        val resp = api.getRates()
        return RatesDomain(
            base = resp.base,
            quotes = resp.rates,
            timestamp = System.currentTimeMillis()
        )
    }
}

class BalancesRepositoryImpl : BalancesRepository {
    private var balances = BalancesDomain(values = mapOf("EUR" to 1000.0))

    override fun getBalances(): BalancesDomain = balances

    override fun update(balances: BalancesDomain) {
        val sanitized = balances.values.mapValues { (_, value) -> maxOf(0.0, value) }
        this.balances = BalancesDomain(sanitized)
    }
}