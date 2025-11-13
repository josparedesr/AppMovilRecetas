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
import kotlinx.coroutines.launch

class BuscarActivity : AppCompatActivity() {

    private var listaDeRecetas: List<Meal>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.buscar_activity_main)

        val edBuscar: EditText = findViewById(R.id.ed_buscar)
        val btnBuscar: Button = findViewById(R.id.btn_buscar)
        val lvResultado: ListView = findViewById(R.id.lv_resultado)

        btnBuscar.setOnClickListener {
            // Convertimos a minúsculas, ¡buena práctica!
            val ingrediente = edBuscar.text.toString().trim().lowercase()
            if (ingrediente.isNotEmpty()) {
                buscarRecetas(ingrediente, lvResultado)
            } else {
                Toast.makeText(this, "Por favor, ingresa un ingrediente", Toast.LENGTH_SHORT).show()
            }
        }

        lvResultado.setOnItemClickListener { parent, view, position, id ->
            val recetaSeleccionada = listaDeRecetas?.get(position)

            // --- CAMBIO AQUÍ ---
            // Ahora usamos 'idMeal' en lugar de 'id'
            if (recetaSeleccionada?.idMeal != null) {
                val intent = Intent(this, DetalleRecetaActivity::class.java)
                // --- CAMBIO AQUÍ ---
                intent.putExtra("MEAL_ID", recetaSeleccionada.idMeal)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: Esta receta no tiene ID", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun buscarRecetas(ingrediente: String, listView: ListView) {
        Toast.makeText(this, "Buscando recetas...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.api.buscarPorIngrediente(ingrediente)

                // --- CAMBIO AQUÍ ---
                // Filtramos usando los nombres nuevos del JSON
                listaDeRecetas = respuesta.meals?.filter {
                    !it.idMeal.isNullOrBlank() && !it.strMeal.isNullOrBlank()
                }

                if (listaDeRecetas.isNullOrEmpty()) {
                    Toast.makeText(this@BuscarActivity, "No se encontraron recetas", Toast.LENGTH_SHORT).show()
                    listView.adapter = null
                } else {
                    // --- CAMBIO AQUÍ ---
                    // Mapeamos usando 'strMeal' en lugar de 'nombre'
                    val nombresDeRecetas = listaDeRecetas!!.map { it.strMeal!! }

                    val adaptador = ArrayAdapter(
                        this@BuscarActivity,
                        android.R.layout.simple_list_item_1,
                        nombresDeRecetas
                    )
                    listView.adapter = adaptador
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@BuscarActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}