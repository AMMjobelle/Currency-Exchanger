package com.jsbanez.currencyexchanger

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

/**
 * Currency Exchange Rates API
 * Provides real-time currency exchange rates
 */
interface RatesApi {
    @GET("currency-exchange-rates")
    suspend fun getRates(): RatesResponse
}

@JsonClass(generateAdapter = true)
data class RatesResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

object RatesServiceProvider {
    private const val BASE_URL = "https://developers.paysera.com/tasks/api/"

    val api: RatesApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
            .create(RatesApi::class.java)
    }
}
