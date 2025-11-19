plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

android {
    namespace = "com.example.apprecetas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.apprecetas"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Núcleo de Retrofit: cliente HTTP para consumir APIs REST con interfaces anotadas (@GET, @POST, etc.)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Convertidor JSON <-> data classes usando Moshi dentro de Retrofit (MoshiConverterFactory)
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    // Interceptor de OkHttp para loguear requests/responses (útil en debug)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // Coroutines Soporte de coroutines en Android (Dispatcher.Main, launch en UI, etc.)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // Extensiones KTX de Lifecycle: lifecycleScope, repeatOnLifecycle y helpers para coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    // Adaptadores de Moshi para Kotlin (null-safety, data classes, KotlinJsonAdapterFactory)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    // Libreria para cargar imagenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Para Corutinas con Room
    ksp("androidx.room:room-compiler:$room_version") // Compilador de Room
    // Google ML Kit para traducción
    implementation("com.google.mlkit:translate:17.0.2")
}