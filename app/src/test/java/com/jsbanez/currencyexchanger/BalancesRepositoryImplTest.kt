package com.jsbanez.currencyexchanger

import org.junit.Assert.*
import org.junit.Test

class BalancesRepositoryImplTest {

    @Test
    fun getBalances_returnsInitialState() {
        // Given
        val repository = BalancesRepositoryImpl()

        // When
        val result = repository.getBalances()

        // Then
        assertEquals(mapOf("EUR" to 1000.0), result.values)
    }

    @Test
    fun update_changesBalances() {
        // Given
        val repository = BalancesRepositoryImpl()
        val newBalances = BalancesDomain(
            values = mapOf(
                "EUR" to 500.0,
                "USD" to 200.0,
                "GBP" to 100.0
            )
        )

        // When
        repository.update(newBalances)
        val result = repository.getBalances()

        // Then
        assertEquals(newBalances.values, result.values)
    }

    @Test
    fun update_replacesAllBalances() {
        // Given
        val repository = BalancesRepositoryImpl()
        val firstUpdate = BalancesDomain(
            values = mapOf("EUR" to 500.0, "USD" to 200.0)
        )
        val secondUpdate = BalancesDomain(
            values = mapOf("GBP" to 300.0, "JPY" to 1000.0)
        )

        // When
        repository.update(firstUpdate)
        repository.update(secondUpdate)
        val result = repository.getBalances()

        // Then
        assertEquals(secondUpdate.values, result.values)
        assertFalse(
            "EUR should not be present after second update",
            result.values.containsKey("EUR")
        )
        assertFalse(
            "USD should not be present after second update",
            result.values.containsKey("USD")
        )
    }

    @Test
    fun update_handlesEmptyBalances() {
        // Given
        val repository = BalancesRepositoryImpl()
        val emptyBalances = BalancesDomain(values = emptyMap())

        // When
        repository.update(emptyBalances)
        val result = repository.getBalances()

        // Then
        assertTrue("Balances should be empty", result.values.isEmpty())
    }

    @Test
    fun update_handlesNegativeBalances() {
        // Given
        val repository = BalancesRepositoryImpl()
        val negativeBalances = BalancesDomain(
            values = mapOf(
                "EUR" to -50.0,
                "USD" to 100.0
            )
        )

        // When
        repository.update(negativeBalances)
        val result = repository.getBalances()

        // Then
        assertEquals(-50.0, result.values["EUR"]!!, 1e-9)
        assertEquals(100.0, result.values["USD"]!!, 1e-9)
    }

    @Test
    fun update_handlesZeroBalances() {
        // Given
        val repository = BalancesRepositoryImpl()
        val zeroBalances = BalancesDomain(
            values = mapOf(
                "EUR" to 0.0,
                "USD" to 0.0
            )
        )

        // When
        repository.update(zeroBalances)
        val result = repository.getBalances()

        // Then
        assertEquals(0.0, result.values["EUR"]!!, 1e-9)
        assertEquals(0.0, result.values["USD"]!!, 1e-9)
    }
}