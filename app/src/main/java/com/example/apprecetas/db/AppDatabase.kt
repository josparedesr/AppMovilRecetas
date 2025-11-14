package com.example.apprecetas.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Esta clase abstracta es la base de datos Room.
 * Le decimos qué "Entidades" (tablas) tiene.
 */
@Database(entities = [RecetaFavorita::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recetaDao(): RecetaDao

    companion object {

        @Volatile
        private var INSTANCIA: AppDatabase? = null
        fun getDatabase(contexto: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    "app_recetas_db"
                )
                    .fallbackToDestructiveMigration()//por probar
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}