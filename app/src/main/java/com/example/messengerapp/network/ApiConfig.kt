package com.example.messengerapp.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.chuckerteam.chucker.api.ChuckerInterceptor

object ApiConfig {

    private var BASE_URL = "https://fcm.googleapis.com"

    fun getApiService(context: Context): ApiService {
        val loggingInterceptor = ChuckerInterceptor.Builder(context)
            .build()

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
