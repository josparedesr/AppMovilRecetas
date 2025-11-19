package com.example.apprecetas

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun applyLanguage(context: Context) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "en") ?: "en"

        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return prefs.getString("My_Lang", "en") ?: "en"
    }
}