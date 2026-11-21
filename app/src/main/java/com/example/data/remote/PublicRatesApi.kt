package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenErApiResponse(
    @Json(name = "result") val result: String? = null,
    @Json(name = "base_code") val baseCode: String? = null,
    @Json(name = "time_last_update_unix") val timeLastUpdateUnix: Long? = null,
    @Json(name = "rates") val rates: Map<String, Double>? = null
)

@JsonClass(generateAdapter = true)
data class FrankfurterResponse(
    @Json(name = "amount") val amount: Double? = null,
    @Json(name = "base") val base: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "rates") val rates: Map<String, Double>? = null
)

@JsonClass(generateAdapter = true)
data class OfflineRatePack(
    @Json(name = "format") val format: String = "minimal_fx_v1",
    @Json(name = "base") val base: String = "USD",
    @Json(name = "updatedAt") val updatedAt: Long,
    @Json(name = "source") val source: String,
    @Json(name = "rates") val rates: Map<String, Double>
)

interface PublicRatesApi {

    /**
     * Public ExchangeRate-API Open Access (no API key required, 160+ world currencies).
     */
    @GET("https://open.er-api.com/v6/latest/USD")
    suspend fun fetchOpenErApiRates(): OpenErApiResponse

    /**
     * Frankfurter Public API (European Central Bank official reference rates, no API key required).
     */
    @GET("https://api.frankfurter.app/latest?from=USD")
    suspend fun fetchFrankfurterRates(): FrankfurterResponse

    companion object {
        fun createMoshi(): Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        fun create(moshi: Moshi = createMoshi()): PublicRatesApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://open.er-api.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(PublicRatesApi::class.java)
        }
    }
}
