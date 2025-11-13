package com.example.apprecetas.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Esta función ya la tenías (para buscar por ingrediente)
    @GET("api/json/v1/1/filter.php")
    suspend fun buscarPorIngrediente(
        @Query("i") ingrediente: String
    ): MealResponse

    // --- ¡AÑADE ESTA NUEVA FUNCIÓN! ---
    // La usamos para buscar los detalles de UNA receta por su ID
    // (Ej: .../lookup.php?i=52772)
    @GET("api/json/v1/1/lookup.php")
    suspend fun buscarDetallePorId(
        @Query("i") id: String
    ): DetalleResponse // Fíjate que devuelve un "DetalleResponse"
}