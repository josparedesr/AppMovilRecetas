package com.example.apprecetas.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // URL base de la API
    private const val BASE_URL = "https://www.themealdb.com/"

    // 1. Configurar el interceptor de logueo
    // (Este lo incluiste en tus dependencias, ¡es muy útil!)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Muestra todo el cuerpo de la respuesta
    }

    // 2. Configurar el cliente OkHttp para que use el interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // 3. Configurar Moshi (el convertidor de JSON)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 4. Configurar Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // <-- Usamos el cliente con logueo
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // 5. Creamos la instancia de nuestro servicio
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}