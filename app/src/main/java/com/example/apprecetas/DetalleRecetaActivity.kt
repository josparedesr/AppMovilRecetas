package com.example.apprecetas

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.apprecetas.api.DetalleMeal
import com.example.apprecetas.api.RetrofitClient
import com.example.apprecetas.db.AppDatabase // ¡Importamos la BD!
import com.example.apprecetas.db.RecetaFavorita // ¡Importamos la Entidad!
import kotlinx.coroutines.launch

class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var ivFoto: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvIngredientes: TextView
    private lateinit var tvInstrucciones: TextView
    private lateinit var btnFavoritos: Button

    // Variable para guardar la receta actual
    private var recetaActual: DetalleMeal? = null

    // Variable para acceder al DAO (menú de la BD)
    private val dao by lazy {
        AppDatabase.getDatabase(this).recetaDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_receta)

        ivFoto = findViewById(R.id.iv_foto_receta)
        tvTitulo = findViewById(R.id.tv_titulo_receta)
        tvIngredientes = findViewById(R.id.tx_ingredientes)
        tvInstrucciones = findViewById(R.id.tx_instrucciones)
        btnFavoritos = findViewById(R.id.btn_agregarFavotitos)

        val mealId = intent.getStringExtra("MEAL_ID")

        if (mealId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: ID de receta no encontrado", Toast.LENGTH_LONG).show()
            finish()
        } else {
            cargarDetalleReceta(mealId)
            // (Opcional) Revisamos si esta receta ya es favorita
            revisarSiEsFavorita(mealId)
        }

        // --- ¡AQUÍ ESTÁ LA NUEVA LÓGICA DE GUARDADO! ---
        btnFavoritos.setOnClickListener {
            // Nos aseguramos de que ya tengamos una receta cargada
            if (recetaActual == null) {
                Toast.makeText(this, "Espera a que cargue la receta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llamamos a la función que guarda en la BD
            guardarRecetaFavorita()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Revisa la BD y actualiza el botón si la receta ya está guardada.
     */
    private fun revisarSiEsFavorita(id: String) {
        lifecycleScope.launch {
            val receta = dao.obtenerPorId(id)
            if (receta != null) {
                // Si la receta ya existe, actualizamos el botón
                btnFavoritos.text = "✅ Ya está en Favoritos"
                btnFavoritos.isEnabled = false // Desactivamos el botón
            }
        }
    }

    /**
     * Llama a la API (esto ya lo tenías)
     */
    private fun cargarDetalleReceta(id: String) {
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.api.buscarDetallePorId(id)
                val receta = respuesta.meals?.firstOrNull()
                recetaActual = receta

                if (recetaActual != null) {
                    tvTitulo.text = recetaActual!!.strMeal
                    tvInstrucciones.text = recetaActual!!.strInstructions

                    Glide.with(this@DetalleRecetaActivity)
                        .load(recetaActual!!.strMealThumb)
                        .into(ivFoto)

                    val ingredientesTexto = construirListaIngredientes(recetaActual!!)
                    tvIngredientes.text = ingredientesTexto
                } else {
                    Toast.makeText(this@DetalleRecetaActivity, "Error: Receta no encontrada", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DetalleRecetaActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Construye los ingredientes (esto ya lo tenías)
     */
    private fun construirListaIngredientes(receta: DetalleMeal): String {
        val builder = StringBuilder()
        if (!receta.strIngredient1.isNullOrBlank()) builder.append("• ${receta.strMeasure1} ${receta.strIngredient1}\n")
        if (!receta.strIngredient2.isNullOrBlank()) builder.append("• ${receta.strMeasure2} ${receta.strIngredient2}\n")
        if (!receta.strIngredient3.isNullOrBlank()) builder.append("• ${receta.strMeasure3} ${receta.strIngredient3}\n")
        if (!receta.strIngredient4.isNullOrBlank()) builder.append("• ${receta.strMeasure4} ${receta.strIngredient4}\n")
        if (!receta.strIngredient5.isNullOrBlank()) builder.append("• ${receta.strMeasure5} ${receta.strIngredient5}\n")
        return builder.toString()
    }

    /**
     * ¡NUEVA FUNCIÓN!
     * Convierte el objeto de la API a un objeto de la BD y lo guarda.
     */
    private fun guardarRecetaFavorita() {
        // Creamos un objeto RecetaFavorita (de la BD) usando
        // los datos de recetaActual (de la API)
        val recetaParaGuardar = RecetaFavorita(
            idMeal = recetaActual!!.idMeal!!, // Sabemos que no es nulo
            strMeal = recetaActual!!.strMeal,
            strMealThumb = recetaActual!!.strMealThumb
        )

        // Lanzamos una corutina para guardar en la BD (es una operación "suspend")
        lifecycleScope.launch {
            try {
                dao.insertarReceta(recetaParaGuardar)

                // Si todo sale bien, mostramos un Toast y actualizamos el botón
                Toast.makeText(this@DetalleRecetaActivity, "¡Guardado en Favoritos!", Toast.LENGTH_SHORT).show()
                btnFavoritos.text = "✅ Guardado"
                btnFavoritos.isEnabled = false

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DetalleRecetaActivity, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}