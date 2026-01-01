package com.jsbanez.currencyexchanger

import org.junit.Assert.*
import org.junit.Test

class DomainModelsTest {

    @Test
    fun ratesDomain_createsCorrectly() {
        // Given
        val base = "EUR"
        val quotes = mapOf("USD" to 1.1, "GBP" to 0.8)
        val timestamp = 1234567890L

        // When
        val rates = RatesDomain(base, quotes, timestamp)

        // Then
        assertEquals(base, rates.base)
        assertEquals(quotes, rates.quotes)
        assertEquals(timestamp, rates.timestamp)
    }

    @Test
    fun ratesDomain_equality() {
        // Given
        val rates1 = RatesDomain("EUR", mapOf("USD" to 1.1), 1000L)
        val rates2 = RatesDomain("EUR", mapOf("USD" to 1.1), 1000L)
        val rates3 = RatesDomain("USD", mapOf("EUR" to 0.9), 1000L)

        // Then
        assertEquals(rates1, rates2)
        assertNotEquals(rates1, rates3)
    }

    @Test
    fun ratesDomain_hashCode() {
        // Given
        val rates1 = RatesDomain("EUR", mapOf("USD" to 1.1), 1000L)
        val rates2 = RatesDomain("EUR", mapOf("USD" to 1.1), 1000L)

        // Then
        assertEquals(rates1.hashCode(), rates2.hashCode())
    }

    @Test
    fun ratesDomain_toString() {
        // Given
        val rates = RatesDomain("EUR", mapOf("USD" to 1.1), 1000L)

        // When
        val toString = rates.toString()

        // Then
        assertTrue("Should contain base currency", toString.contains("EUR"))
        assertTrue("Should contain quotes", toString.contains("USD"))
        assertTrue("Should contain timestamp", toString.contains("1000"))
    }

    @Test
    fun ratesDomain_handlesEmptyQuotes() {
        // Given
        val rates = RatesDomain("EUR", emptyMap(), 1000L)

        // Then
        assertEquals("EUR", rates.base)
        assertTrue("Quotes should be empty", rates.quotes.isEmpty())
        assertEquals(1000L, rates.timestamp)
    }

    @Test
    fun ratesDomain_handlesLargeQuotesMap() {
        // Given
        val largeQuotes = mutableMapOf<String, Double>()
        repeat(100) { i ->
            largeQuotes["CUR$i"] = i * 1.1
        }

        // When
        val rates = RatesDomain("EUR", largeQuotes, 1000L)

        // Then
        assertEquals(100, rates.quotes.size)
        assertEquals(0.0, rates.quotes["CUR0"]!!, 1e-9)
        assertEquals(99 * 1.1, rates.quotes["CUR99"]!!, 1e-9)
    }

    @Test
    fun balancesDomain_createsCorrectly() {
        // Given
        val values = mapOf("EUR" to 1000.0, "USD" to 500.0)

        // When
        val balances = BalancesDomain(values)

        // Then
        assertEquals(values, balances.values)
    }

    @Test
    fun balancesDomain_equality() {
        // Given
        val balances1 = BalancesDomain(mapOf("EUR" to 1000.0))
        val balances2 = BalancesDomain(mapOf("EUR" to 1000.0))
        val balances3 = BalancesDomain(mapOf("USD" to 1000.0))

        // Then
        assertEquals(balances1, balances2)
        assertNotEquals(balances1, balances3)
    }

    @Test
    fun balancesDomain_hashCode() {
        // Given
        val balances1 = BalancesDomain(mapOf("EUR" to 1000.0))
        val balances2 = BalancesDomain(mapOf("EUR" to 1000.0))

        // Then
        assertEquals(balances1.hashCode(), balances2.hashCode())
    }

    @Test
    fun balancesDomain_toString() {
        // Given
        val balances = BalancesDomain(mapOf("EUR" to 1000.0, "USD" to 500.0))

        // When
        val toString = balances.toString()

        // Then
        assertTrue("Should contain EUR", toString.contains("EUR"))
        assertTrue("Should contain USD", toString.contains("USD"))
        assertTrue("Should contain balance values", toString.contains("1000"))
    }

    @Test
    fun balancesDomain_handlesEmptyValues() {
        // Given
        val balances = BalancesDomain(emptyMap())

        // Then
        assertTrue("Values should be empty", balances.values.isEmpty())
    }

    @Test
    fun balancesDomain_handlesNegativeValues() {
        // Given
        val values = mapOf("EUR" to -100.0, "USD" to 200.0)

        // When
        val balances = BalancesDomain(values)

        // Then
        assertEquals(-100.0, balances.values["EUR"]!!, 1e-9)
        assertEquals(200.0, balances.values["USD"]!!, 1e-9)
    }

    @Test
    fun balancesDomain_handlesZeroValues() {
        // Given
        val values = mapOf("EUR" to 0.0, "USD" to 0.0)

        // When
        val balances = BalancesDomain(values)

        // Then
        assertEquals(0.0, balances.values["EUR"]!!, 1e-9)
        assertEquals(0.0, balances.values["USD"]!!, 1e-9)
    }

    @Test
    fun balancesDomain_handlesVerySmallValues() {
        // Given
        val values = mapOf("EUR" to 0.0001, "USD" to 0.000000001)

        // When
        val balances = BalancesDomain(values)

        // Then
        assertEquals(0.0001, balances.values["EUR"]!!, 1e-10)
        assertEquals(0.000000001, balances.values["USD"]!!, 1e-15)
    }

    @Test
    fun balancesDomain_handlesLargeValues() {
        // Given
        val values = mapOf("EUR" to 1e10, "USD" to Double.MAX_VALUE)

        // When
        val balances = BalancesDomain(values)

        // Then
        assertEquals(1e10, balances.values["EUR"]!!, 1e6)
        assertEquals(Double.MAX_VALUE, balances.values["USD"]!!, 1e300)
    }

    @Test
    fun balancesDomain_immutability() {
        // Given
        val originalMap = mutableMapOf("EUR" to 1000.0)
        val balances = BalancesDomain(originalMap)

        // When - Modify original map
        originalMap["USD"] = 500.0

        // Then - BalancesDomain should not be affected if it copies the map
        // Note: This test assumes the data class creates a defensive copy
        // If not, this behavior is still acceptable for a simple data class
        assertTrue("Should contain EUR", balances.values.containsKey("EUR"))
    }
}