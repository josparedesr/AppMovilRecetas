package com.example.apprecetas

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    // --- Variables de clase ---
    private lateinit var lvFavoritos: ListView
    private lateinit var tvTituloFavoritos: TextView

    // Variable para acceder al DAO (menú de la BD)
    private val dao: RecetaDao by lazy {
        AppDatabase.getDatabase(this).recetaDao()
    }

    // Variable para guardar la lista de favoritos
    // (la necesitamos para saber en qué item se hizo clic)
    private var listaDeFavoritos: List<RecetaFavorita> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Cargamos el XML que diseñaste (el #4)
        setContentView(R.layout.activity_favoritos)

        // 1. Conectamos los componentes del XML
        lvFavoritos = findViewById(R.id.lv_favoritos)
        tvTituloFavoritos = findViewById(R.id.tx_favoritos)

        // 2. Programamos el clic de la lista de favoritos
        lvFavoritos.setOnItemClickListener { parent, view, position, id ->
            // Obtenemos la receta de nuestra lista local
            val recetaSeleccionada = listaDeFavoritos[position]

            // Creamos un intent para abrir el Detalle
            val intent = Intent(this, DetalleRecetaActivity::class.java)

            // Le pasamos el ID, igual que en BuscarActivity
            intent.putExtra("MEAL_ID", recetaSeleccionada.idMeal)

            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * onResume() se llama CADA VEZ que la pantalla se vuelve visible.
     * Esto es mejor que onCreate() porque si el usuario guarda un favorito
     * y presiona "Atrás", la lista se actualizará al instante.
     */
    override fun onResume() {
        super.onResume()
        // Llamamos a la función que carga los datos de la BD
        cargarFavoritos()
    }

    /**
     * Función que lee la BD (en segundo plano) y actualiza el ListView
     */
    private fun cargarFavoritos() {
        lifecycleScope.launch {
            try {
                // 1. ¡Leemos la base de datos!
                listaDeFavoritos = dao.obtenerTodas()

                // 2. Verificamos si encontramos algo
                if (listaDeFavoritos.isEmpty()) {
                    // Si no hay favoritos, mostramos un mensaje
                    tvTituloFavoritos.text = "No tienes recetas favoritas"
                    lvFavoritos.adapter = null // Limpiamos la lista
                } else {
                    // Si SÍ hay favoritos...
                    tvTituloFavoritos.text = "Mis Recetas Favoritas"

                    // 3. Extraemos solo los nombres para el adaptador
                    val nombresFavoritos = listaDeFavoritos.map { it.strMeal }

                    // 4. Creamos y asignamos el adaptador
                    val adaptador = ArrayAdapter(
                        this@FavoritosActivity,
                        android.R.layout.simple_list_item_1,
                        nombresFavoritos
                    )
                    lvFavoritos.adapter = adaptador
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@FavoritosActivity, "Error al cargar favoritos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}