package com.example.apprecetas

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Aplicamos el idioma ANTES de cargar la vista
        cargarIdioma()

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnBuscar: ExtendedFloatingActionButton = findViewById(R.id.btn_buscar)
        val btnFavoritos: ExtendedFloatingActionButton = findViewById(R.id.btn_favoritos)
        val btnLenguaje: FloatingActionButton = findViewById(R.id.btn_lenguaje)
        val txBienvenida: TextView = findViewById(R.id.tx_bienvenido)

        // Configurar listeners
        btnBuscar.setOnClickListener {
            startActivity(Intent(this, BuscarActivity::class.java))
        }

        btnFavoritos.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        // 2. Lógica del botón de idioma
        btnLenguaje.setOnClickListener {
            cambiarIdioma()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarIdioma() {
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "en") ?: "en"
        setLocale(language)
    }

    private fun cambiarIdioma() {
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en")

        // Si es inglés cambiamos a español, y viceversa
        val newLang = if (currentLang == "en") "es" else "en"

        // Guardamos
        val editor = prefs.edit()
        editor.putString("My_Lang", newLang)
        editor.apply()

        // Aplicamos y recargamos
        setLocale(newLang)
        recreate() // ¡Esto reinicia la actividad para ver los cambios!
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}