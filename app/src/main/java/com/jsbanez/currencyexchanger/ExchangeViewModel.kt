package com.jsbanez.currencyexchanger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.net.UnknownHostException
import java.net.ConnectException
import java.net.SocketTimeoutException

open class ExchangeViewModel(private val context: Context? = null) : ViewModel() {

    data class UiState(
        val balances: Map<String, Double> = mapOf("EUR" to 1000.0),
        val sellCurrency: String = "EUR",
        val buyCurrency: String = "USD",
        val inputAmount: String = "",
        val ratesBase: String = "EUR",
        val rates: Map<String, Double> = emptyMap(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val lastUpdatedMillis: Long? = null,
        val showNetworkDialog: Boolean = false,
        val isNetworkAvailable: Boolean = true
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var pollingJob: Job? = null

    private val networkManager = context?.let { NetworkConnectivityManager(it) }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        val isNetworkError = when (throwable) {
            is UnknownHostException, is ConnectException, is SocketTimeoutException -> true
            else -> false
        }

        if (isNetworkError && networkManager?.isNetworkAvailable() == false) {
            _state.update { it.copy(isLoading = false, error = null, showNetworkDialog = true) }
        } else {
            _state.update { it.copy(isLoading = false, error = throwable.message) }
        }
    }

    private val ratesApi = RatesServiceProvider.api
    private val ratesRepository: RatesRepository = RatesRepositoryImpl(ratesApi)
    private val balancesRepository: BalancesRepository = BalancesRepositoryImpl()
    private val getRates = GetRatesUseCase(ratesRepository)
    private val converter = ConvertAmountUseCase()
    private val exchanger = PerformExchangeUseCase(balancesRepository, converter)

    init {
        startRatesPolling()
        _state.update { it.copy(balances = balancesRepository.getBalances().values) }

        networkManager?.let { manager ->
            viewModelScope.launch {
                manager.observeNetworkState().collect { isConnected ->
                    _state.update { it.copy(isNetworkAvailable = isConnected) }
                    if (!isConnected) {
                        _state.update { it.copy(showNetworkDialog = true) }
                    }
                }
            }
        }
    }

    fun setSellCurrency(code: String) {
        _state.update { it.copy(sellCurrency = code) }
    }

    fun setBuyCurrency(code: String) {
        _state.update { it.copy(buyCurrency = code) }
    }

    fun setInputAmount(value: String) {
        val sanitized = value.replace(',', '.').filter { it.isDigit() || it == '.' }
        _state.update { it.copy(inputAmount = sanitized) }
    }

    fun swapCurrencies() {
        _state.update { it.copy(sellCurrency = it.buyCurrency, buyCurrency = it.sellCurrency) }
    }

    fun dismissNetworkDialog() {
        _state.update { it.copy(showNetworkDialog = false) }
    }

    fun retryNetworkOperation() {
        if (networkManager?.isNetworkAvailable() == true) {
            _state.update { it.copy(showNetworkDialog = false) }
            refreshNow()
        } else {
            _state.update { it.copy(showNetworkDialog = true) }
        }
    }

    fun availableCurrencies(): List<String> {
        val set = (state.value.balances.keys + state.value.rates.keys + state.value.ratesBase).toMutableSet()
        return set.filter { it.length == 3 }.sorted()
    }

    fun formattedBalance(code: String): String {
        val amount = state.value.balances[code] ?: 0.0
        return "$code ${df.format(amount)}"
    }

    fun computeQuote(): Double? {
        val amount = state.value.inputAmount.toDoubleOrNull() ?: return null
        val ratesDomain = RatesDomain(state.value.ratesBase, state.value.rates, state.value.lastUpdatedMillis ?: 0L)
        return converter.convert(amount, state.value.sellCurrency, state.value.buyCurrency, ratesDomain)
    }

    fun canExchange(): Boolean {
        val amount = state.value.inputAmount.toDoubleOrNull() ?: return false
        if (amount <= 0) return false
        return exchanger.canExchange(amount, state.value.sellCurrency, state.value.buyCurrency)
    }

    fun performExchange(): Boolean {
        if (networkManager?.isNetworkAvailable() == false || !state.value.isNetworkAvailable) {
            _state.update { it.copy(showNetworkDialog = true) }
            return false
        }

        val amount = state.value.inputAmount.toDoubleOrNull() ?: return false
        if (amount <= 0) return false
        val sell = state.value.sellCurrency
        val buy = state.value.buyCurrency
        val ratesDomain = RatesDomain(state.value.ratesBase, state.value.rates, state.value.lastUpdatedMillis ?: 0L)
        val ok = exchanger.perform(amount, sell, buy, ratesDomain)
        if (ok) {
            _state.update { it.copy(balances = balancesRepository.getBalances().values, inputAmount = "") }
        }
        return ok
    }

    private fun startRatesPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO + handler) {
            while (true) {
                fetchRatesOnce()
                delay(5_000)
            }
        }
    }

    fun refreshNow() {
        viewModelScope.launch(Dispatchers.IO + handler) { fetchRatesOnce() }
    }

    private suspend fun fetchRatesOnce() {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val resp = getRates()
            _state.update {
                it.copy(
                    ratesBase = resp.base,
                    rates = resp.quotes,
                    isLoading = false,
                    lastUpdatedMillis = resp.timestamp
                )
            }
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.message) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    companion object {
        private val df = DecimalFormat("#,##0.00")
    }
}
