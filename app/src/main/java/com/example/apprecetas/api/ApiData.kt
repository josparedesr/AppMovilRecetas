package com.example.apprecetas.api

// ¡YA NO NECESITAMOS IMPORTAR @Json!

// --- PARTE 1: CLASES PARA BÚSQUEDA ---
data class Meal(
    // Las variables se llaman IGUAL que en el JSON
    val idMeal: String?,
    val strMeal: String?,
    val strMealThumb: String?
)

data class MealResponse(
    val meals: List<Meal>?
)

// --- PARTE 2: CLASES PARA DETALLE ---
data class DetalleMeal(
    val idMeal: String?,
    val strMeal: String?,
    val strMealThumb: String?,
    val strInstructions: String?,

    // Ingredientes
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,

    // Medidas
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?
)

data class DetalleResponse(
    val meals: List<DetalleMeal>?
)