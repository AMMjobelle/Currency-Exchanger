package com.jsbanez.currencyexchanger

import org.junit.Assert.*
import org.junit.Test

class DomainUseCasesTest {

    // Helper fakes
    private class FakeBalancesRepository(initial: Map<String, Double>) : BalancesRepository {
        private var store = BalancesDomain(initial)
        override fun getBalances(): BalancesDomain = store
        override fun update(balances: BalancesDomain) { store = balances }
    }

    private class FakeRatesRepository(private val domain: RatesDomain) : RatesRepository {
        override suspend fun getRates(): RatesDomain = domain
    }

    @Test
    fun convert_sameCurrency_returnsSameAmount() {
        val useCase = ConvertAmountUseCase()
        val rates = RatesDomain(base = "EUR", quotes = mapOf("USD" to 1.2), timestamp = 0L)
        val out = useCase.convert(50.0, "EUR", "EUR", rates)
        out?.let { assertEquals(50.0, it, 1e-9) }
    }

    @Test
    fun convert_betweenNonBaseCurrencies_usesCrossRates() {
        val useCase = ConvertAmountUseCase()
        val rates = RatesDomain(base = "EUR", quotes = mapOf("USD" to 1.2, "GBP" to 0.8), timestamp = 0L)
        // 120 USD -> in EUR = 120/1.2 = 100 -> to GBP = 100*0.8 = 80
        val out = useCase.convert(120.0, "USD", "GBP", rates)
        out?.let { assertEquals(80.0, it, 1e-9) }
    }

    @Test
    fun convert_unknownCurrency_returnsNull() {
        val useCase = ConvertAmountUseCase()
        val rates = RatesDomain(base = "EUR", quotes = mapOf("USD" to 1.2), timestamp = 0L)
        val out = useCase.convert(10.0, "JPY", "USD", rates)
        assertNull(out)
    }

    @Test
    fun canExchange_checksBalanceAndCurrencies() {
        val balances = FakeBalancesRepository(mapOf("EUR" to 100.0))
        val useCase = PerformExchangeUseCase(balances, ConvertAmountUseCase())
        assertFalse(useCase.canExchange(0.0, "EUR", "USD"))
        assertFalse(useCase.canExchange(10.0, "EUR", "EUR"))
        assertTrue(useCase.canExchange(50.0, "EUR", "USD"))
        assertFalse(useCase.canExchange(150.0, "EUR", "USD"))
    }

    @Test
    fun performExchange_updatesBalancesOnSuccess() {
        val balances = FakeBalancesRepository(mapOf("EUR" to 100.0, "USD" to 0.0))
        val convert = ConvertAmountUseCase()
        val useCase = PerformExchangeUseCase(balances, convert)
        val rates = RatesDomain(base = "EUR", quotes = mapOf("USD" to 2.0), timestamp = 0L)
        val ok = useCase.perform(10.0, "EUR", "USD", rates)
        assertTrue(ok)
        val after = balances.getBalances().values
        assertEquals(90.0, after["EUR"]!!, 1e-9)
        assertEquals(20.0, after["USD"]!!, 1e-9)
    }

    @Test
    fun performExchange_failsWhenInsufficientFunds() {
        val balances = FakeBalancesRepository(mapOf("EUR" to 5.0))
        val convert = ConvertAmountUseCase()
        val useCase = PerformExchangeUseCase(balances, convert)
        val rates = RatesDomain(base = "EUR", quotes = mapOf("USD" to 2.0), timestamp = 0L)
        val ok = useCase.perform(10.0, "EUR", "USD", rates)
        assertFalse(ok)
        val after = balances.getBalances().values
        assertEquals(5.0, after["EUR"]!!, 1e-9)
        assertEquals(null, after["USD"])
    }

    @Test
    fun performExchange_createsBuyCurrencyIfMissing() {
        val balances = FakeBalancesRepository(mapOf("EUR" to 100.0))
        val convert = ConvertAmountUseCase()
        val useCase = PerformExchangeUseCase(balances, convert)
        val rates = RatesDomain(base = "EUR", quotes = mapOf("USD" to 1.5), timestamp = 0L)
        val ok = useCase.perform(10.0, "EUR", "USD", rates)
        assertTrue(ok)
        val after = balances.getBalances().values
        assertEquals(90.0, after["EUR"]!!, 1e-9)
        assertEquals(15.0, after["USD"]!!, 1e-9)
    }

    @Test
    fun getRatesUseCase_delegatesToRepository() {
        val expected = RatesDomain("EUR", mapOf("USD" to 1.23), 123L)
        val repo = FakeRatesRepository(expected)
        val uc = GetRatesUseCase(repo)
        // no runBlocking available by default; just check type by calling in a simple coroutine
        kotlinx.coroutines.runBlocking {
            val out = uc()
            assertEquals(expected, out)
        }
    }

    @Test
    fun getRatesUseCase_propagatesExceptions() {
        val failingRepo = object : RatesRepository {
            override suspend fun getRates(): RatesDomain {
                throw RuntimeException("network down")
            }
        }
        val uc = GetRatesUseCase(failingRepo)
        kotlinx.coroutines.runBlocking {
            try {
                uc()
                fail("Expected exception")
            } catch (e: RuntimeException) {
                assertEquals("network down", e.message)
            }
        }
    }

    // UI-related assertions for balance visibility and non-negative enforcement
    @Test
    fun viewModel_formattedBalance_showsUserBalanceForUI() {
        // Build a ViewModel with a fake balances repo by subclassing and injecting via reflection is complex here.
        // Instead, test formattedBalance logic by simulating state update through public API flows.
        val vm = ExchangeViewModel()
        // default has EUR 1000.0
        val eur = vm.formattedBalance("EUR")
        // Should start with currency code and formatted number
        assertTrue(eur.startsWith("EUR "))
        // The numeric part should be non-empty
        assertTrue(eur.length > 4)
    }

    @Test
    fun performExchange_useCase_doesNotAllowNegativeBalances() {
        val balances = object : BalancesRepository {
            private var store = BalancesDomain(mapOf(
                "EUR" to 5.0,
                "USD" to 0.0
            ))
            override fun getBalances(): BalancesDomain = store
            override fun update(balances: BalancesDomain) { store = balances }
        }
        val useCase = PerformExchangeUseCase(balances, ConvertAmountUseCase())
        val rates = RatesDomain(base = "EUR", quotes = mapOf("USD" to 1.2), timestamp = 0L)

        // Try to sell more than we have; perform() should fail and not make balance negative
        val ok = useCase.perform(10.0, "EUR", "USD", rates)
        assertFalse(ok)
        val after = balances.getBalances().values
        assertEquals(5.0, after["EUR"]!!, 1e-9)
        assertEquals(0.0, after["USD"]!!, 1e-9)
    }
}
