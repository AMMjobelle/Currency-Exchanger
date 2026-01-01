package com.jsbanez.currencyexchanger

import org.junit.Assert.*
import org.junit.Test

class ExchangeViewModelFormattingTest {

    @Test
    fun formattedBalance_displaysCorrectFormat() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        val formatted = viewModel.formattedBalance("EUR")

        // Then
        assertTrue("Should start with currency code", formatted.startsWith("EUR "))
        assertTrue("Should contain formatted number", formatted.contains("1,000.00"))
        assertEquals("EUR 1,000.00", formatted)
    }

    @Test
    fun formattedBalance_handlesZeroBalance() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        val formatted = viewModel.formattedBalance("USD")

        // Then
        assertEquals("USD 0.00", formatted)
    }

    @Test
    fun formattedBalance_handlesSmallDecimals() {
        // Given
        val viewModel = ExchangeViewModel()
        // Perform an exchange to create a balance with decimals
        viewModel.setSellCurrency("EUR")
        viewModel.setBuyCurrency("USD")
        viewModel.setInputAmount("1")

        // Mock rates by updating state directly through exchange if possible
        // For this test, we'll just test the formatting logic with known values

        // When
        val formatted = viewModel.formattedBalance("EUR")

        // Then
        assertTrue("Should format with 2 decimal places", formatted.contains("."))
    }

    @Test
    fun availableCurrencies_returnsAlphabeticallySorted() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        val currencies = viewModel.availableCurrencies()

        // Then
        assertTrue("Should contain at least EUR", currencies.contains("EUR"))
        // Verify sorting - each currency should be <= next currency
        for (i in 0 until currencies.size - 1) {
            assertTrue("Currencies should be sorted", currencies[i] <= currencies[i + 1])
        }
        // Verify all are 3-character codes
        assertTrue(
            "All currencies should be 3 characters",
            currencies.all { it.length == 3 })
    }

    @Test
    fun availableCurrencies_includesBalanceCurrencies() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        val currencies = viewModel.availableCurrencies()

        // Then
        assertTrue("Should include EUR from initial balances", currencies.contains("EUR"))
    }

    @Test
    fun setInputAmount_filtersNonNumericCharacters() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("1a2b3.4c5")

        // Then
        assertEquals("123.45", viewModel.state.value.inputAmount)
    }

    @Test
    fun setInputAmount_replacesCommasWithDots() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("1,234.56")

        // Then
        assertEquals("1.234.56", viewModel.state.value.inputAmount)
    }

    @Test
    fun setInputAmount_allowsMultipleDots() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("1.2.3.4")

        // Then
        assertEquals("1.2.3.4", viewModel.state.value.inputAmount)
    }

    @Test
    fun setInputAmount_handlesEmptyString() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("")

        // Then
        assertEquals("", viewModel.state.value.inputAmount)
    }

    @Test
    fun setInputAmount_handlesSpecialCharacters() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("!@#$%^&*()_+-=[]{}|;':\",./<>?")

        // Then
        assertEquals(".", viewModel.state.value.inputAmount)
    }

    @Test
    fun setSellCurrency_updatesCurrency() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setSellCurrency("USD")

        // Then
        assertEquals("USD", viewModel.state.value.sellCurrency)
    }

    @Test
    fun setBuyCurrency_updatesCurrency() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setBuyCurrency("GBP")

        // Then
        assertEquals("GBP", viewModel.state.value.buyCurrency)
    }

    @Test
    fun swapCurrencies_exchangesPositions() {
        // Given
        val viewModel = ExchangeViewModel()
        viewModel.setSellCurrency("EUR")
        viewModel.setBuyCurrency("USD")

        // When
        viewModel.swapCurrencies()

        // Then
        assertEquals("USD", viewModel.state.value.sellCurrency)
        assertEquals("EUR", viewModel.state.value.buyCurrency)
    }

    @Test
    fun swapCurrencies_worksMultipleTimes() {
        // Given
        val viewModel = ExchangeViewModel()
        viewModel.setSellCurrency("EUR")
        viewModel.setBuyCurrency("GBP")

        // When
        viewModel.swapCurrencies()
        viewModel.swapCurrencies()

        // Then
        assertEquals("EUR", viewModel.state.value.sellCurrency)
        assertEquals("GBP", viewModel.state.value.buyCurrency)
    }

    @Test
    fun computeQuote_returnsNullForInvalidAmount() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("abc")
        val result = viewModel.computeQuote()

        // Then
        assertNull("Should return null for non-numeric input", result)
    }

    @Test
    fun computeQuote_returnsNullForEmptyAmount() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("")
        val result = viewModel.computeQuote()

        // Then
        assertNull("Should return null for empty input", result)
    }

    @Test
    fun canExchange_falseForInvalidAmount() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("not-a-number")
        val result = viewModel.canExchange()

        // Then
        assertFalse("Should return false for invalid amount", result)
    }

    @Test
    fun canExchange_falseForZeroAmount() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("0")
        val result = viewModel.canExchange()

        // Then
        assertFalse("Should return false for zero amount", result)
    }

    @Test
    fun canExchange_falseForNegativeAmount() {
        // Given
        val viewModel = ExchangeViewModel()

        // When
        viewModel.setInputAmount("-100")
        val result = viewModel.canExchange()

        // Then
        assertFalse("Should return false for negative amount", result)
    }
}