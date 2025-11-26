package com.example.apprecetas.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/json/v1/1/filter.php")
    suspend fun buscarPorIngrediente(
        @Query("i") ingrediente: String
    ): MealResponse

    @GET("api/json/v1/1/lookup.php")
    suspend fun buscarDetallePorId(
        @Query("i") id: String
    ): DetalleResponse

    @GET("api/json/v1/1/random.php")
    suspend fun obtenerRecetaAleatoria(): DetalleResponse
}