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
import com.example.apprecetas.api.DetalleMeal // Este no cambia
import com.example.apprecetas.api.RetrofitClient
import kotlinx.coroutines.launch

class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var ivFoto: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvIngredientes: TextView
    private lateinit var tvInstrucciones: TextView
    private lateinit var btnFavoritos: Button

    private var recetaActual: DetalleMeal? = null

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
        }

        btnFavoritos.setOnClickListener {
            Toast.makeText(this, "Función (Guardar) aún no implementada", Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarDetalleReceta(id: String) {
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.api.buscarDetallePorId(id)
                val receta = respuesta.meals?.firstOrNull()
                recetaActual = receta

                if (recetaActual != null) {
                    // --- CAMBIOS AQUÍ ---
                    // Usamos los nombres directos del JSON
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

    private fun construirListaIngredientes(receta: DetalleMeal): String {
        val builder = StringBuilder()
        if (!receta.strIngredient1.isNullOrBlank()) builder.append("• ${receta.strMeasure1} ${receta.strIngredient1}\n")
        if (!receta.strIngredient2.isNullOrBlank()) builder.append("• ${receta.strMeasure2} ${receta.strIngredient2}\n")
        if (!receta.strIngredient3.isNullOrBlank()) builder.append("• ${receta.strMeasure3} ${receta.strIngredient3}\n")
        if (!receta.strIngredient4.isNullOrBlank()) builder.append("• ${receta.strMeasure4} ${receta.strIngredient4}\n")
        if (!receta.strIngredient5.isNullOrBlank()) builder.append("• ${receta.strMeasure5} ${receta.strIngredient5}\n")
        return builder.toString()
    }
}