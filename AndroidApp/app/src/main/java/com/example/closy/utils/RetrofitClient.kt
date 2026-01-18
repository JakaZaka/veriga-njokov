package com.example.closy.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.closy.BuildConfig

object RetrofitClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = ""

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    // Funkcija, ki dobi ali ustvari nov API servis glede na URL
    fun getApi(baseUrl: String): ClothingApiService {
        // Poskrbimo, da se URL konča s /
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        // Če se URL spremeni, ustvarimo nov Retrofit objekt
        if (retrofit == null || currentBaseUrl != formattedUrl) {
            currentBaseUrl = formattedUrl
            retrofit = Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ClothingApiService::class.java)
    }

    // Za kompatibilnost: uporabi port 5001, ker tam teče tvoj Flask
    val api: ClothingApiService
        get() = getApi("http://${BuildConfig.SERVER_IP}:5001/")
}