package com.jsbanez.currencyexchanger

import org.junit.Assert.*
import org.junit.Test

class ConvertAmountUseCaseTest {

    private val useCase = ConvertAmountUseCase()

    @Test
    fun convert_fromBaseToQuoteCurrency_usesDirectRate() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.2, "GBP" to 0.8),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(100.0, "EUR", "USD", rates)

        // Then
        assertEquals(120.0, result!!, 1e-9)
    }

    @Test
    fun convert_fromQuoteCurrencyToBase_dividesByRate() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.2, "GBP" to 0.8),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(120.0, "USD", "EUR", rates)

        // Then
        assertEquals(100.0, result!!, 1e-9)
    }

    @Test
    fun convert_crossRateCalculation_correctConversion() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 2.0, "GBP" to 1.0, "JPY" to 100.0),
            timestamp = 0L
        )

        // When - Convert 200 USD to 50 JPY (via EUR)
        // 200 USD -> 100 EUR -> 10000 JPY
        val result = useCase.convert(200.0, "USD", "JPY", rates)

        // Then
        assertEquals(10000.0, result!!, 1e-9)
    }

    @Test
    fun convert_zeroAmount_returnsZero() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.2),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(0.0, "EUR", "USD", rates)

        // Then
        assertEquals(0.0, result!!, 1e-9)
    }

    @Test
    fun convert_negativeAmount_handlesCorrectly() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.5),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(-100.0, "EUR", "USD", rates)

        // Then
        assertEquals(-150.0, result!!, 1e-9)
    }

    @Test
    fun convert_verySmallAmount_maintainsPrecision() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.123456789),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(0.000001, "EUR", "USD", rates)

        // Then
        assertEquals(0.000001123456789, result!!, 1e-15)
    }

    @Test
    fun convert_veryLargeAmount_handlesCorrectly() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.1),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(1000000000.0, "EUR", "USD", rates)

        // Then
        assertEquals(1100000000.0, result!!, 1e-6)
    }

    @Test
    fun convert_rateOfOne_returnsEqualAmount() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.0),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(500.0, "EUR", "USD", rates)

        // Then
        assertEquals(500.0, result!!, 1e-9)
    }

    @Test
    fun convert_fractionalRate_calculatesCorrectly() {
        // Given
        val rates = RatesDomain(
            base = "USD",
            quotes = mapOf("EUR" to 0.85, "GBP" to 0.75),
            timestamp = 0L
        )

        // When - Convert 100 EUR to GBP via USD
        // 100 EUR -> 100/0.85 = ~117.65 USD -> 117.65 * 0.75 = ~88.24 GBP
        val result = useCase.convert(100.0, "EUR", "GBP", rates)

        // Then
        val expected = (100.0 / 0.85) * 0.75
        assertEquals(expected, result!!, 1e-9)
    }

    @Test
    fun convert_multipleUnknownCurrencies_returnsNull() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 1.2),
            timestamp = 0L
        )

        // When
        val result = useCase.convert(100.0, "XYZ", "ABC", rates)

        // Then
        assertNull(result)
    }

    @Test
    fun convert_emptyQuotes_onlyWorksWithBaseCurrency() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = emptyMap(),
            timestamp = 0L
        )

        // When - Same currency conversion should work
        val sameCurrencyResult = useCase.convert(100.0, "EUR", "EUR", rates)
        val unknownResult = useCase.convert(100.0, "EUR", "USD", rates)

        // Then
        assertEquals(100.0, sameCurrencyResult!!, 1e-9)
        assertNull(unknownResult)
    }

    @Test
    fun convert_zeroRate_handlesCorrectly() {
        // Given
        val rates = RatesDomain(
            base = "EUR",
            quotes = mapOf("USD" to 0.0, "GBP" to 1.0),
            timestamp = 0L
        )

        // When - Division by zero should be handled gracefully
        val fromZeroRateResult = useCase.convert(100.0, "USD", "EUR", rates)
        val toZeroRateResult = useCase.convert(100.0, "EUR", "USD", rates)

        // Then
        // From currency with zero rate: division by zero -> should return infinity or null
        assertTrue(
            "Should handle division by zero",
            fromZeroRateResult == null || fromZeroRateResult!!.isInfinite()
        )
        assertEquals(0.0, toZeroRateResult!!, 1e-9)
    }
}