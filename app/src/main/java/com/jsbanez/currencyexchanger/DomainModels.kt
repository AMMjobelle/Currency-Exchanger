package com.jsbanez.currencyexchanger

data class RatesDomain(
    val base: String,
    val quotes: Map<String, Double>,
    val timestamp: Long
)

data class BalancesDomain(
    val values: Map<String, Double>
)
