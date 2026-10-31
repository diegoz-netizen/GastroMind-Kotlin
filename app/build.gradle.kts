import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// URL del backend Spring. Por defecto, el emulador (10.0.2.2 = localhost de la PC).
// En una tablet real: gastromind.apiUrl=http://<IP de la PC>:8080/ en local.properties o en gradle.properties del usuario.
val propiedadesLocales = Properties().apply {
    val local = rootProject.file("local.properties")
    if (local.exists()) local.inputStream().use { load(it) }
}
val apiUrl: String = propiedadesLocales.getProperty("gastromind.apiUrl")
    ?: providers.gradleProperty("gastromind.apiUrl").orNull
    ?: "http://10.0.2.2:8080/"
// URL del backend Django (kardex, HU-15). Se configura igual: gastromind.djangoUrl=http://<IP de la PC>:8000/
val djangoUrl: String = propiedadesLocales.getProperty("gastromind.djangoUrl")
    ?: providers.gradleProperty("gastromind.djangoUrl").orNull
    ?: "http://10.0.2.2:8000/"

android {
    namespace = "com.gastromind.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.gastromind.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "API_URL", "\"$apiUrl\"")
        buildConfigField("String", "DJANGO_URL", "\"$djangoUrl\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Sesión (token y rol) en DataStore
    implementation(libs.androidx.datastore.preferences)

    // API de Spring: Retrofit + Gson + OkHttp (el JWT va en un interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
