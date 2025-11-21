package com.example.apprecetas.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO (Data Access Object)
 * Define las funciones para interactuar con la base de datos.
 */
@Dao
interface RecetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReceta(receta: RecetaFavorita)

    @Query("SELECT * FROM recetas_favoritas")
    suspend fun obtenerTodas(): List<RecetaFavorita>

    @Query("SELECT * FROM recetas_favoritas WHERE idMeal = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): RecetaFavorita?

    @Delete
    suspend fun eliminarReceta(receta: RecetaFavorita)
}