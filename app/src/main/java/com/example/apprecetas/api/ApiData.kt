package com.example.apprecetas.api

import com.squareup.moshi.Json

data class Meal(
    @field:Json(name = "idMeal") val id: String,
    @field:Json(name = "strMeal") val nombre: String,
    @field:Json(name = "strMealThumb") val imagenUrl: String
)

data class MealResponse(
    @field:Json(name = "meals") val meals: List<Meal>?
)