package com.example.apprecetas.api

//Clase para busqueda
data class Meal(
    val idMeal: String?,
    val strMeal: String?,
    val strMealThumb: String?
)

data class MealResponse(
    val meals: List<Meal>?
)

//Clase para el Detalle
data class DetalleMeal(
    val idMeal: String?,
    val strMeal: String?,
    val strMealThumb: String?,
    val strInstructions: String?,

    val strYoutube: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,

    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?
)

data class DetalleResponse(
    val meals: List<DetalleMeal>?
)