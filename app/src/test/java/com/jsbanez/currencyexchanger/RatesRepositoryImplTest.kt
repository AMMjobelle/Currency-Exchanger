package com.jsbanez.currencyexchanger

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RatesRepositoryImplTest {

    private class FakeRatesApi(
        private val response: RatesResponse? = null,
        private val exception: Exception? = null
    ) : RatesApi {
        override suspend fun getRates(): RatesResponse {
            exception?.let { throw it }
            return response ?: throw IllegalStateException("No response configured")
        }
    }

    @Test
    fun getRates_mapsApiResponseToDomain() {
        runBlocking {
            // Given
            val apiResponse = RatesResponse(
                base = "EUR",
                date = "2023-12-25",
                rates = mapOf("USD" to 1.1056, "GBP" to 0.8642)
            )
            val fakeApi = FakeRatesApi(response = apiResponse)
            val repository = RatesRepositoryImpl(fakeApi)

            // When
            val result = repository.getRates()

            // Then
            assertEquals("EUR", result.base)
            assertEquals(mapOf("USD" to 1.1056, "GBP" to 0.8642), result.quotes)
            assertTrue("Timestamp should be set", result.timestamp > 0)
        }
    }

    @Test
    fun getRates_propagatesApiExceptions() {
        runBlocking {
            // Given
            val exception = RuntimeException("Network error")
            val fakeApi = FakeRatesApi(exception = exception)
            val repository = RatesRepositoryImpl(fakeApi)

            // When & Then
            try {
                repository.getRates()
                fail("Expected exception to be thrown")
            } catch (e: RuntimeException) {
                assertEquals("Network error", e.message)
            }
        }
    }

    @Test
    fun getRates_handlesEmptyRates() {
        runBlocking {
            // Given
            val apiResponse = RatesResponse(
                base = "EUR",
                date = "2023-12-25",
                rates = emptyMap()
            )
            val fakeApi = FakeRatesApi(response = apiResponse)
            val repository = RatesRepositoryImpl(fakeApi)

            // When
            val result = repository.getRates()

            // Then
            assertEquals("EUR", result.base)
            assertTrue("Quotes should be empty", result.quotes.isEmpty())
            assertTrue("Timestamp should be set", result.timestamp > 0)
        }
    }

    @Test
    fun getRates_setsCurrentTimestamp() {
        runBlocking {
            // Given
            val apiResponse = RatesResponse(
                base = "USD",
                date = "2023-12-25",
                rates = mapOf("EUR" to 0.85, "GBP" to 0.78)
            )
            val fakeApi = FakeRatesApi(response = apiResponse)
            val repository = RatesRepositoryImpl(fakeApi)
            val beforeCall = System.currentTimeMillis()

            // When
            val result = repository.getRates()

            // Then
            val afterCall = System.currentTimeMillis()
            assertTrue(
                "Timestamp should be within call timeframe",
                result.timestamp >= beforeCall && result.timestamp <= afterCall
            )
        }
    }
}