package com.example.apprecetas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.apprecetas.api.Meal
import com.example.apprecetas.api.RetrofitClient
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch

class BuscarActivity : AppCompatActivity() {

    private var listaDeRecetas: List<Meal>? = null

    private val diccionarioLatino = mapOf(
        "carne" to "beef",
        "res" to "beef",
        "bistec" to "beef",
        "ternera" to "beef",
        "chancho" to "pork",
        "cerdo" to "pork",
        "puerco" to "pork",
        "pollo" to "chicken",
        "pescado" to "fish",
        "camarones" to "shrimp",
        "gamba" to "shrimp",
        "palta" to "avocado",
        "aguacate" to "avocado",
        "queso" to "cheese",
        "papa" to "Floury potatoes"
    )
    // --------------------------------------------------

    private val translatorEsToEn = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SPANISH)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
    )

    private val translatorEnToEs = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applyLanguage(this)
        enableEdgeToEdge()
        setContentView(R.layout.buscar_activity_main)

        descargarModelos()

        val edBuscar: EditText = findViewById(R.id.ed_buscar)
        val btnBuscar: Button = findViewById(R.id.btn_buscar)
        val lvResultado: ListView = findViewById(R.id.lv_resultado)

        btnBuscar.setOnClickListener {
            // Convertimos a minúsculas para buscar en el diccionario
            val textoUsuario = edBuscar.text.toString().trim().lowercase()

            if (textoUsuario.isNotEmpty()) {
                val idiomaActual = LocaleHelper.getLanguage(this)

                if (idiomaActual == "es") {
                    // --- LÓGICA LATINA ---
                    // 1. Primero revisamos nuestro diccionario manual
                    if (diccionarioLatino.containsKey(textoUsuario)) {
                        // ¡Bingo! Está en el diccionario (ej: carne -> beef)
                        val terminoCorregido = diccionarioLatino[textoUsuario]!!
                        // Buscamos directo con la palabra corregida
                        buscarRecetas(terminoCorregido, lvResultado, true)
                    } else {
                        // 2. Si no está en el diccionario, usamos el Traductor de Google
                        Toast.makeText(this, getString(R.string.toast_searching), Toast.LENGTH_SHORT).show() // "Buscando..."

                        translatorEsToEn.translate(textoUsuario)
                            .addOnSuccessListener { textoTraducido ->
                                buscarRecetas(textoTraducido, lvResultado, true)
                            }
                            .addOnFailureListener {
                                buscarRecetas(textoUsuario, lvResultado, false)
                            }
                    }
                } else {
                    buscarRecetas(textoUsuario, lvResultado, false)
                }
            } else {
                Toast.makeText(this, getString(R.string.error_empty), Toast.LENGTH_SHORT).show()
            }
        }

        lvResultado.setOnItemClickListener { parent, view, position, id ->
            val recetaSeleccionada = listaDeRecetas?.get(position)

            if (recetaSeleccionada?.idMeal != null) {
                val intent = Intent(this, DetalleRecetaActivity::class.java)
                intent.putExtra("MEAL_ID", recetaSeleccionada.idMeal)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error ID", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun descargarModelos() {
        val conditions = DownloadConditions.Builder().requireWifi().build()
        translatorEsToEn.downloadModelIfNeeded(conditions)
        translatorEnToEs.downloadModelIfNeeded(conditions)
    }

    private fun buscarRecetas(ingrediente: String, listView: ListView, traducirResultados: Boolean) {

        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.api.buscarPorIngrediente(ingrediente)

                listaDeRecetas = respuesta.meals?.filter {
                    !it.idMeal.isNullOrBlank() && !it.strMeal.isNullOrBlank()
                }

                if (listaDeRecetas.isNullOrEmpty()) {
                    Toast.makeText(this@BuscarActivity, getString(R.string.error_no_results), Toast.LENGTH_SHORT).show()
                    listView.adapter = null
                } else {
                    val nombresOriginales = listaDeRecetas!!.map { it.strMeal!! }

                    if (traducirResultados) {
                        val adaptador = ArrayAdapter(
                            this@BuscarActivity,
                            android.R.layout.simple_list_item_1,
                            mutableListOf<String>()
                        )
                        listView.adapter = adaptador

                        for (nombreIngles in nombresOriginales) {
                            translatorEnToEs.translate(nombreIngles)
                                .addOnSuccessListener { nombreEspanol ->
                                    adaptador.add(nombreEspanol)
                                }
                                .addOnFailureListener {
                                    adaptador.add(nombreIngles)
                                }
                        }
                    } else {
                        val adaptador = ArrayAdapter(
                            this@BuscarActivity,
                            android.R.layout.simple_list_item_1,
                            nombresOriginales
                        )
                        listView.adapter = adaptador
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}