package com.example.apprecetas

import android.os.Bundle
import android.view.View // ¡Importante para la visibilidad!
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
import com.example.apprecetas.db.AppDatabase
import com.example.apprecetas.db.RecetaFavorita
import kotlinx.coroutines.launch

class DetalleRecetaActivity : AppCompatActivity() {

    // --- CAMBIO ---
    // Añadimos la variable para el nuevo botón
    private lateinit var ivFoto: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvIngredientes: TextView
    private lateinit var tvInstrucciones: TextView
    private lateinit var btnFavoritos: Button
    private lateinit var btnEliminar: Button // <-- ¡NUEVA!

    private var recetaActual: DetalleMeal? = null
    private val dao by lazy { AppDatabase.getDatabase(this).recetaDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_receta)

        // --- CAMBIO ---
        // Conectamos el nuevo botón
        ivFoto = findViewById(R.id.iv_foto_receta)
        tvTitulo = findViewById(R.id.tv_titulo_receta)
        tvIngredientes = findViewById(R.id.tx_ingredientes)
        tvInstrucciones = findViewById(R.id.tx_instrucciones)
        btnFavoritos = findViewById(R.id.btn_agregarFavotitos)
        btnEliminar = findViewById(R.id.btn_eliminarFavoritos) // <-- ¡NUEVA!

        val mealId = intent.getStringExtra("MEAL_ID")
        if (mealId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: ID de receta no encontrado", Toast.LENGTH_LONG).show()
            finish()
        } else {
            cargarDetalleReceta(mealId)
            revisarSiEsFavorita(mealId)
        }

        // Lógica del botón AGREGAR
        btnFavoritos.setOnClickListener {
            recetaActual?.let {
                guardarRecetaFavorita(it)
            }
        }

        // --- CAMBIO ---
        // ¡Añadimos la lógica del botón ELIMINAR!
        btnEliminar.setOnClickListener {
            recetaActual?.let {
                eliminarRecetaFavorita(it)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Revisa la BD y MUESTRA/OCULTA el botón correcto.
     */
    private fun revisarSiEsFavorita(id: String) {
        lifecycleScope.launch {
            val receta = dao.obtenerPorId(id)
            if (receta != null) {
                // Si la receta ya existe, OCULTAMOS "Agregar" y MOSTRAMOS "Eliminar"
                btnFavoritos.visibility = View.GONE
                btnEliminar.visibility = View.VISIBLE
            } else {
                // Si no existe, MOSTRAMOS "Agregar" y OCULTAMOS "Eliminar"
                btnFavoritos.visibility = View.VISIBLE
                btnEliminar.visibility = View.GONE
            }
        }
    }

    /**
     * Guarda la receta y actualiza la visibilidad de los botones
     */
    private fun guardarRecetaFavorita(receta: DetalleMeal) {
        // Creamos el objeto para la BD
        val recetaParaGuardar = RecetaFavorita(
            idMeal = receta.idMeal!!,
            strMeal = receta.strMeal,
            strMealThumb = receta.strMealThumb
        )

        lifecycleScope.launch {
            try {
                dao.insertarReceta(recetaParaGuardar)
                Toast.makeText(this@DetalleRecetaActivity, "¡Guardado en Favoritos!", Toast.LENGTH_SHORT).show()

                // --- CAMBIO ---
                // Ocultamos "Agregar" y mostramos "Eliminar"
                btnFavoritos.visibility = View.GONE
                btnEliminar.visibility = View.VISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DetalleRecetaActivity, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * ¡NUEVA FUNCIÓN!
     * Elimina la receta y actualiza la visibilidad de los botones
     */
    private fun eliminarRecetaFavorita(receta: DetalleMeal) {
        // Creamos el objeto (con el ID) que queremos eliminar
        val recetaParaEliminar = RecetaFavorita(
            idMeal = receta.idMeal!!,
            strMeal = receta.strMeal,
            strMealThumb = receta.strMealThumb
        )

        lifecycleScope.launch {
            try {
                dao.eliminarReceta(recetaParaEliminar)
                Toast.makeText(this@DetalleRecetaActivity, "Eliminado de Favoritos", Toast.LENGTH_SHORT).show()

                // --- CAMBIO ---
                // Ocultamos "Eliminar" y mostramos "Agregar"
                btnEliminar.visibility = View.GONE
                btnFavoritos.visibility = View.VISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DetalleRecetaActivity, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- (El resto de funciones no cambian) ---

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
                    tvIngredientes.text = construirListaIngredientes(recetaActual!!)
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