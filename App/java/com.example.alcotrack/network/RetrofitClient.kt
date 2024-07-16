package com.example.alcotrack.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

  object RetrofitClient {
    private const val BASE_URL = "http://192.168.158.182/alcotrack/" //

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

