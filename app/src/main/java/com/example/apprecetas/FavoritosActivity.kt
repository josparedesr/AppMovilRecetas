package com.example.apprecetas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.apprecetas.db.AppDatabase
import com.example.apprecetas.db.RecetaDao
import com.example.apprecetas.db.RecetaFavorita
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    private lateinit var lvFavoritos: ListView
    private lateinit var tvTituloFavoritos: TextView

    private val dao: RecetaDao by lazy {
        AppDatabase.getDatabase(this).recetaDao()
    }

    private var listaDeFavoritos: List<RecetaFavorita> = emptyList()

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
        setContentView(R.layout.activity_favoritos)

        translatorEnToEs.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build())

        lvFavoritos = findViewById(R.id.lv_favoritos)
        tvTituloFavoritos = findViewById(R.id.tx_favoritos)

        tvTituloFavoritos.text = getString(R.string.title_favorites)

        lvFavoritos.setOnItemClickListener { parent, view, position, id ->
            val recetaSeleccionada = listaDeFavoritos[position]
            val intent = Intent(this, DetalleRecetaActivity::class.java)
            intent.putExtra("MEAL_ID", recetaSeleccionada.idMeal)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    private fun cargarFavoritos() {
        lifecycleScope.launch {
            try {
                listaDeFavoritos = dao.obtenerTodas()

                if (listaDeFavoritos.isEmpty()) {
                    tvTituloFavoritos.text = getString(R.string.empty_favorites)
                    lvFavoritos.adapter = null
                } else {
                    tvTituloFavoritos.text = getString(R.string.title_favorites)

                    val nombresParaMostrar = listaDeFavoritos.map { it.strMeal ?: "" }.toMutableList()

                    val adaptador = ArrayAdapter(
                        this@FavoritosActivity,
                        android.R.layout.simple_list_item_1,
                        nombresParaMostrar
                    )
                    lvFavoritos.adapter = adaptador

                    if (LocaleHelper.getLanguage(this@FavoritosActivity) == "es") {
                        listaDeFavoritos.forEachIndexed { index, receta ->
                            val nombreOriginal = receta.strMeal ?: ""

                            translatorEnToEs.translate(nombreOriginal)
                                .addOnSuccessListener { nombreTraducido ->
                                    if (index < nombresParaMostrar.size) {
                                        nombresParaMostrar[index] = nombreTraducido
                                        adaptador.notifyDataSetChanged()
                                    }
                                }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@FavoritosActivity, "Error al cargar favoritos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}