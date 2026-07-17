package com.example.data.api

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class TickerPrice(
    val symbol: String,
    val price: String
)

interface BinanceApi {
    @GET("api/v3/ticker/price")
    suspend fun getTickerPrices(): List<TickerPrice>
}

object BinanceClient {
    private const val BASE_URL = "https://api.binance.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: BinanceApi = retrofit.create(BinanceApi::class.java)
}
