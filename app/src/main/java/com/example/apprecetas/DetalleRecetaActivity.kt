package com.example.apprecetas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.apprecetas.api.DetalleMeal
import com.example.apprecetas.api.RetrofitClient
import com.example.apprecetas.db.AppDatabase
import com.example.apprecetas.db.RecetaFavorita
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private var uriCamaraTemporal: Uri? = null

    private lateinit var btnYoutube: Button
    private lateinit var btnShare: Button

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
            mostrarYGuardarFoto(uri)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && uriCamaraTemporal != null) {
            mostrarYGuardarFoto(uriCamaraTemporal!!)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            abrirCamara()
        } else if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true ||
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
            abrirGaleria()
        } else {
            // Usamos el texto traducible
            Toast.makeText(this, getString(R.string.toast_permissions_needed), Toast.LENGTH_SHORT).show()
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
        btnYoutube = findViewById(R.id.btn_youtube)
        btnShare = findViewById(R.id.btn_share)

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
            mostrarDialogoSeleccion()
        }

        btnYoutube.setOnClickListener {
            val url = recetaActual?.strYoutube
            if (!url.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.error_no_video), Toast.LENGTH_SHORT).show()
            }
        }

        btnShare.setOnClickListener {
            recetaActual?.let { receta ->
                // Preparamos el mensaje
                val mensajeBase = getString(R.string.share_text)
                // Usamos los ingredientes que ya construimos en el TextView
                val ingredientes = tvIngredientes.text.toString()
                // Formateamos el mensaje (Nombre + Ingredientes)
                val mensajeFinal = String.format(mensajeBase, receta.strMeal, ingredientes)

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, mensajeFinal)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mostrarDialogoSeleccion() {
        // --- CORRECCIÓN: Usamos getString para que se traduzca ---
        val opciones = arrayOf(
            getString(R.string.option_camera),  // "Tomar Foto" o "Take Photo"
            getString(R.string.option_gallery)  // "Elegir de Galería" o "Choose from Gallery"
        )
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_photo_title)) // Título traducido
        builder.setItems(opciones) { _, which ->
            when (which) {
                0 -> revisarPermisoCamara()
                1 -> revisarPermisoGaleria()
            }
        }
        builder.show()
    }

    private fun revisarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun abrirCamara() {
        try {
            val photoFile = crearArchivoDeImagen()
            uriCamaraTemporal = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(uriCamaraTemporal)
        } catch (e: Exception) {
            // Texto traducido
            Toast.makeText(this, getString(R.string.error_camera), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun crearArchivoDeImagen(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun revisarPermisoGaleria() {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria()
        } else {
            requestPermissionLauncher.launch(arrayOf(permiso))
        }
    }

    private fun abrirGaleria() {
        galleryLauncher.launch(arrayOf("image/*"))
    }

    private fun mostrarYGuardarFoto(uri: Uri) {
        ivMiFoto.setImageURI(uri)
        favoritoActual?.let { fav ->
            val favoritoActualizado = fav.copy(miFotoUri = uri.toString())
            lifecycleScope.launch {
                dao.insertarReceta(favoritoActualizado)
                // Texto traducido
                Toast.makeText(this@DetalleRecetaActivity, getString(R.string.toast_photo_saved), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun intentarTraducirReceta(receta: DetalleMeal, ingredientesTexto: String) {
        val idiomaActual = LocaleHelper.getLanguage(this)
        if (idiomaActual != "es") return

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().requireWifi().build()

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener {
            receta.strMeal?.let { translator.translate(it).addOnSuccessListener { t -> tvTitulo.text = t } }
            receta.strInstructions?.let { translator.translate(it).addOnSuccessListener { t -> tvInstrucciones.text = t } }
            translator.translate(ingredientesTexto).addOnSuccessListener { t -> tvIngredientes.text = t }
        }
    }

    private fun cargarDetalleReceta(id: String) {
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.api.buscarDetallePorId(id)
                val receta = respuesta.meals?.firstOrNull()
                recetaActual = receta

                if (recetaActual != null) {
                    tvTitulo.text = recetaActual!!.strMeal
                    tvInstrucciones.text = recetaActual!!.strInstructions
                    val ingredientes = construirListaIngredientes(recetaActual!!)
                    tvIngredientes.text = ingredientes
                    Glide.with(this@DetalleRecetaActivity).load(recetaActual!!.strMealThumb).into(ivFoto)

                    intentarTraducirReceta(recetaActual!!, ingredientes)
                } else {
                    Toast.makeText(this@DetalleRecetaActivity, getString(R.string.error_no_results), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) { e.printStackTrace() }
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
                    try { ivMiFoto.setImageURI(Uri.parse(receta.miFotoUri)) } catch (e: Exception) {}
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
        val nuevo = RecetaFavorita(idMeal = receta.idMeal!!, strMeal = receta.strMeal, strMealThumb = receta.strMealThumb)
        lifecycleScope.launch {
            dao.insertarReceta(nuevo)
            Toast.makeText(this@DetalleRecetaActivity, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show()
            revisarSiEsFavorita(receta.idMeal!!)
        }
    }

    private fun eliminarRecetaFavorita(receta: RecetaFavorita) {
        lifecycleScope.launch {
            dao.eliminarReceta(receta)
            Toast.makeText(this@DetalleRecetaActivity, getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show()
            revisarSiEsFavorita(receta.idMeal)
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