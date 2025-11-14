package com.example.apprecetas.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO (Data Access Object)
 * Define las funciones para interactuar con la base de datos.
 */
@Dao
interface RecetaDao {

    // "suspend" significa que se debe llamar desde una Corutina
    // OnConflictStrategy.REPLACE significa que si intentas insertar
    // una receta con un ID que ya existe, la reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReceta(receta: RecetaFavorita)

    // Esta consulta nos devolverá todas las recetas de la tabla
    @Query("SELECT * FROM recetas_favoritas")
    suspend fun obtenerTodas(): List<RecetaFavorita>

    // (Opcional, pero útil) Consulta para saber si una receta YA está guardada
    @Query("SELECT * FROM recetas_favoritas WHERE idMeal = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): RecetaFavorita?
}