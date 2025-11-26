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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.apprecetas.api.RetrofitClient
import android.widget.Toast

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
        val txBienvenido: TextView = findViewById(R.id.tx_bienvenido)
        val btnRandom: ExtendedFloatingActionButton = findViewById(R.id.btn_random) // <-- CONECTAR

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

        btnRandom.setOnClickListener {
            // Mostramos un mensajito rápido
            Toast.makeText(this, "Buscando suerte...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                try {
                    // 1. Llamamos a la API (Random)
                    val respuesta = RetrofitClient.api.obtenerRecetaAleatoria()
                    val receta = respuesta.meals?.firstOrNull()

                    if (receta != null && !receta.idMeal.isNullOrEmpty()) {
                        // 2. Si encontramos una, abrimos el Detalle con ese ID
                        val intent = Intent(this@MainActivity, DetalleRecetaActivity::class.java)
                        intent.putExtra("MEAL_ID", receta.idMeal)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "Intenta de nuevo", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
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