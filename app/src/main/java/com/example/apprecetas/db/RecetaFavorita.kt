package com.example.apprecetas.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Esta es la "Entidad" o "Molde" para la tabla de favoritos.
 */
@Entity(tableName = "recetas_favoritas")
data class RecetaFavorita(
    @PrimaryKey val idMeal: String,
    val strMeal: String?,
    val strMealThumb: String?,

    val miFotoUri: String? = null
)