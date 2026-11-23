package com.mistxyt.netshield_iot.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mistxyt.netshield_iot.data.remote.api.NetShieldApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun sanitizeIp(rawIp: String): String {
        return rawIp.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("ws://")
            .removePrefix("wss://")
            .substringBefore(":")
            .trimEnd('/')
            .trim()
    }

    val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()
    }

    private var currentBaseUrl: String? = null
    private var currentApiService: NetShieldApiService? = null

    @Synchronized
    fun getApiService(ip: String, port: Int): NetShieldApiService {
        val cleanIp = sanitizeIp(ip).ifBlank { "127.0.0.1" }
        val effectivePort = if (port in 1..65535) port else 80
        val baseUrl = "http://$cleanIp:$effectivePort/"

        if (currentApiService != null && currentBaseUrl == baseUrl) {
            return currentApiService!!
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(NetShieldApiService::class.java)
        currentBaseUrl = baseUrl
        currentApiService = service
        return service
    }
}
