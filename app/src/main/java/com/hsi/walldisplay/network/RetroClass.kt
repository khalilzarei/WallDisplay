package com.hsi.walldisplay.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetroClass {
    private var retrofit: Retrofit? = null
    private val url = SessionManager.localJobIp + Constants.API_URL

    private val retrofitInstance: Retrofit
        get() {
            val gson = GsonBuilder().setLenient().create()
            val okHttpClient = OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build()
            return Retrofit.Builder().baseUrl(url).client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
        }

    val apiService: APIService
        get() {
            Log.e("TAG", url)
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val client = OkHttpClient.Builder().readTimeout(2, TimeUnit.MINUTES).writeTimeout(2, TimeUnit.MINUTES)
                .addInterceptor { chain: Interceptor.Chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .method(
                            original.method,
                            original.body
                        )
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }.build()
            if (retrofit == null) {
                retrofit = Retrofit.Builder().baseUrl(url).client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson)).build()
            }
            return retrofit!!.create(APIService::class.java)
        }

    fun apiService(url: String): APIService {
        Log.e("TAG", url)
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val client = OkHttpClient.Builder().readTimeout(2, TimeUnit.MINUTES).writeTimeout(2, TimeUnit.MINUTES)
            .addInterceptor { chain: Interceptor.Chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .method(
                        original.method,
                        original.body
                    )
                val request = requestBuilder.build()
                chain.proceed(request)
            }.build()
        if (retrofit == null) {
            retrofit = Retrofit.Builder().baseUrl(url).client(client)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
        }
        return retrofit!!.create(APIService::class.java)
    }

    val localAPIService: APIService
        get() {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val client = OkHttpClient.Builder().readTimeout(2, TimeUnit.MINUTES).writeTimeout(2, TimeUnit.MINUTES)
                .addInterceptor { chain: Interceptor.Chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .method(
                            original.method,
                            original.body
                        )
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }.build()
            if (retrofit == null) {
                retrofit = Retrofit.Builder().baseUrl(SessionManager.localJobIp.toString()).client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson)).build()
            }
            return retrofit!!.create(APIService::class.java)
        }
}