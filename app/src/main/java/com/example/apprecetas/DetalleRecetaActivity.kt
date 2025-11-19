package com.example.apprecetas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.apprecetas.api.DetalleMeal
import com.example.apprecetas.api.RetrofitClient
import com.example.apprecetas.db.AppDatabase
import com.example.apprecetas.db.RecetaFavorita

// --- NUEVO: IMPORTS PARA LA TRADUCCIÓN (ML KIT) ---
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
// --------------------------------------------------

import kotlinx.coroutines.launch

class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var ivFoto: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvIngredientes: TextView
    private lateinit var tvInstrucciones: TextView
    private lateinit var btnFavoritos: Button
    private lateinit var btnEliminar: Button
    private lateinit var tvLabelMiFoto: TextView
    private lateinit var ivMiFoto: ImageView
    private lateinit var btnAgregarMiFoto: Button

    private var recetaActual: DetalleMeal? = null
    private var favoritoActual: RecetaFavorita? = null
    private val dao by lazy { AppDatabase.getDatabase(this).recetaDao() }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ivMiFoto.setImageURI(uri)
            guardarUriDeFoto(uri)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirGaleria()
        } else {
            Toast.makeText(this, "Permiso de Galería denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.applyLanguage(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_receta)

        ivFoto = findViewById(R.id.iv_foto_receta)
        tvTitulo = findViewById(R.id.tv_titulo_receta)
        tvIngredientes = findViewById(R.id.tx_ingredientes)
        tvInstrucciones = findViewById(R.id.tx_instrucciones)
        btnFavoritos = findViewById(R.id.btn_agregarFavotitos)
        btnEliminar = findViewById(R.id.btn_eliminarFavoritos)
        tvLabelMiFoto = findViewById(R.id.tv_label_mi_foto)
        ivMiFoto = findViewById(R.id.iv_mi_foto)
        btnAgregarMiFoto = findViewById(R.id.btn_agregarMiFoto)

        val mealId = intent.getStringExtra("MEAL_ID")
        if (mealId.isNullOrEmpty()) {
            finish()
        } else {
            cargarDetalleReceta(mealId)
            revisarSiEsFavorita(mealId)
        }

        btnFavoritos.setOnClickListener {
            recetaActual?.let { guardarRecetaFavorita(it) }
        }

        btnEliminar.setOnClickListener {
            favoritoActual?.let { eliminarRecetaFavorita(it) }
        }

        btnAgregarMiFoto.setOnClickListener {
            revisarPermisoYAbrirGaleria()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- NUEVO: FUNCIONES DE TRADUCCIÓN ---

    private fun intentarTraducirReceta(receta: DetalleMeal, ingredientesTexto: String) {
        // 1. Verificamos si el idioma actual de la app es Español ("es")
        val idiomaActual = LocaleHelper.getLanguage(this)
        if (idiomaActual != "es") {
            return // Si está en inglés, no hacemos nada
        }

        // 2. Configuramos el traductor de Inglés a Español
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
        val englishSpanishTranslator = Translation.getClient(options)

        // 3. Condiciones para descargar el modelo (necesita Wifi/Datos)
        val conditions = DownloadConditions.Builder().requireWifi().build()

        // Aviso visual
        Toast.makeText(this, getString(R.string.toast_searching), Toast.LENGTH_SHORT).show() // "Traduciendo..."

        // 4. Descargamos el modelo (si no existe) y traducimos
        englishSpanishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // A. Traducir Título
                receta.strMeal?.let { titulo ->
                    englishSpanishTranslator.translate(titulo)
                        .addOnSuccessListener { traducido -> tvTitulo.text = traducido }
                }
                // B. Traducir Instrucciones
                receta.strInstructions?.let { instrucciones ->
                    englishSpanishTranslator.translate(instrucciones)
                        .addOnSuccessListener { traducido -> tvInstrucciones.text = traducido }
                }
                // C. Traducir Ingredientes
                englishSpanishTranslator.translate(ingredientesTexto)
                    .addOnSuccessListener { traducido -> tvIngredientes.text = traducido }
            }
            .addOnFailureListener {
                // Si falla la descarga (sin internet), se queda en inglés
            }
    }
    // --------------------------------------

    private fun cargarDetalleReceta(id: String) {
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.api.buscarDetallePorId(id)
                val receta = respuesta.meals?.firstOrNull()
                recetaActual = receta

                if (recetaActual != null) {
                    tvTitulo.text = recetaActual!!.strMeal
                    tvInstrucciones.text = recetaActual!!.strInstructions
                    val ingredientesTexto = construirListaIngredientes(recetaActual!!)
                    tvIngredientes.text = ingredientesTexto

                    Glide.with(this@DetalleRecetaActivity)
                        .load(recetaActual!!.strMealThumb)
                        .into(ivFoto)

                    // --- NUEVO: LLAMADA AL TRADUCTOR ---
                    intentarTraducirReceta(recetaActual!!, ingredientesTexto)
                    // -----------------------------------

                } else {
                    Toast.makeText(this@DetalleRecetaActivity, getString(R.string.error_no_results), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun revisarSiEsFavorita(id: String) {
        lifecycleScope.launch {
            val receta = dao.obtenerPorId(id)
            favoritoActual = receta

            if (receta != null) {
                btnFavoritos.visibility = View.GONE
                btnEliminar.visibility = View.VISIBLE
                tvLabelMiFoto.visibility = View.VISIBLE
                ivMiFoto.visibility = View.VISIBLE
                btnAgregarMiFoto.visibility = View.VISIBLE

                if (!receta.miFotoUri.isNullOrEmpty()) {
                    try {
                        ivMiFoto.setImageURI(Uri.parse(receta.miFotoUri))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                btnFavoritos.visibility = View.VISIBLE
                btnEliminar.visibility = View.GONE
                tvLabelMiFoto.visibility = View.GONE
                ivMiFoto.visibility = View.GONE
                btnAgregarMiFoto.visibility = View.GONE
            }
        }
    }

    private fun guardarRecetaFavorita(receta: DetalleMeal) {
        val recetaParaGuardar = RecetaFavorita(
            idMeal = receta.idMeal!!,
            strMeal = receta.strMeal,
            strMealThumb = receta.strMealThumb
        )

        lifecycleScope.launch {
            try {
                dao.insertarReceta(recetaParaGuardar)
                Toast.makeText(this@DetalleRecetaActivity, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
                revisarSiEsFavorita(receta.idMeal!!)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun eliminarRecetaFavorita(receta: RecetaFavorita) {
        lifecycleScope.launch {
            try {
                dao.eliminarReceta(receta)
                Toast.makeText(this@DetalleRecetaActivity, getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show()
                revisarSiEsFavorita(receta.idMeal)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun revisarPermisoYAbrirGaleria() {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED -> {
                abrirGaleria()
            }
            shouldShowRequestPermissionRationale(permiso) -> {
                Toast.makeText(this, "Se necesita permiso para acceder a la galería", Toast.LENGTH_LONG).show()
                permissionLauncher.launch(permiso)
            }
            else -> {
                permissionLauncher.launch(permiso)
            }
        }
    }

    private fun abrirGaleria() {
        galleryLauncher.launch(arrayOf("image/*"))
    }

    private fun guardarUriDeFoto(uri: Uri) {
        favoritoActual?.let { fav ->
            val favoritoActualizado = fav.copy(miFotoUri = uri.toString())
            lifecycleScope.launch {
                try {
                    dao.insertarReceta(favoritoActualizado)
                    Toast.makeText(this@DetalleRecetaActivity, "Foto guardada", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { e.printStackTrace() }
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