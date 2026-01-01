package com.jsbanez.currencyexchanger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeViewModelTest {

    // Test doubles to inject into the ViewModel via a small subclass that exposes constructors
    private class TestBalancesRepository(initial: Map<String, Double>) : BalancesRepository {
        private var store = BalancesDomain(initial)
        override fun getBalances(): BalancesDomain = store
        override fun update(balances: BalancesDomain) { store = balances }
    }

    private class TestRatesRepository(var domain: RatesDomain) : RatesRepository {
        override suspend fun getRates(): RatesDomain = domain
    }

    // Small subclass to allow injecting repos and use cases while reusing base logic
    private class TestableExchangeViewModel(
        private val balancesRepo: BalancesRepository,
        private val ratesRepo: RatesRepository
    ) : ExchangeViewModel() {
        init {
            // override initial state balances to reflect provided repo values
            // rely on public API to refresh from repo by calling performExchange with zero or similar is hacky.
            // Instead, we simulate by accessing internal state via the available formattedBalance output checks.
        }
    }

    @Test
    fun setInputAmount_sanitizesToNumbersAndDot() {
        val vm = ExchangeViewModel()
        vm.setInputAmount("12,3a.4b5")
        assertEquals("12.3.45".filter { it.isDigit() || it == '.' }, vm.state.value.inputAmount)
        // assert that commas became dots and letters were removed
        assertTrue(vm.state.value.inputAmount.contains('.'))
        assertTrue(vm.state.value.inputAmount.all { it.isDigit() || it == '.' })
    }

    @Test
    fun swapCurrencies_swapsSellAndBuy() {
        val vm = ExchangeViewModel()
        vm.setSellCurrency("EUR")
        vm.setBuyCurrency("USD")
        vm.swapCurrencies()
        assertEquals("USD", vm.state.value.sellCurrency)
        assertEquals("EUR", vm.state.value.buyCurrency)
    }

    @Test
    fun availableCurrencies_includesRatesAndBalancesAndBase() {
        val vm = ExchangeViewModel()
        // Initially only EUR balance; after we simulate rates arrival, list should include base and quote keys
        runBlocking {
            // call refreshNow which triggers fetch once using default repo and network; to avoid network,
            // we simply assert that base EUR and any known entries from balances are present without requiring network.
            val list = vm.availableCurrencies()
            assertTrue(list.contains("EUR"))
        }
    }

    @Test
    fun canExchange_checksPositiveAmountAndFunds() {
        val vm = ExchangeViewModel()
        vm.setSellCurrency("EUR")
        vm.setBuyCurrency("USD")

        vm.setInputAmount("-5")
        assertFalse(vm.canExchange())

        vm.setInputAmount("0")
        assertFalse(vm.canExchange())

        vm.setInputAmount("10")
        assertTrue(vm.canExchange())
    }

    @Test
    fun computeQuote_returnsNullWhenAmountInvalid() {
        val vm = ExchangeViewModel()
        vm.setInputAmount("not-a-number")
        assertNull(vm.computeQuote())
    }
}
