package com.example.apprecetas.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/json/v1/1/filter.php")
    suspend fun buscarPorIngrediente(
        @Query("i") ingrediente: String
    ): MealResponse
}