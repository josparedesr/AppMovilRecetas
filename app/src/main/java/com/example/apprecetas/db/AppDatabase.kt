package com.example.apprecetas.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Esta clase abstracta es la base de datos Room.
 * Le decimos qué "Entidades" (tablas) tiene.
 */
@Database(entities = [RecetaFavorita::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Le dice a la BD qué DAOs (menús) tiene
    abstract fun recetaDao(): RecetaDao

    companion object {
        // "Volatile" asegura que esta variable sea siempre
        // la misma para todos los hilos de la app
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        // Esta función nos da la única instancia de la BD
        // (Patrón Singleton)
        fun getDatabase(contexto: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    "app_recetas_db" // Nombre del archivo de la BD
                ).build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}