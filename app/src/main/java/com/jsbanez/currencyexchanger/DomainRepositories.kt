package com.jsbanez.currencyexchanger

interface RatesRepository {
    suspend fun getRates(): RatesDomain
}

interface BalancesRepository {
    fun getBalances(): BalancesDomain
    fun update(balances: BalancesDomain)
}
