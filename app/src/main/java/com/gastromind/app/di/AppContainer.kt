package com.gastromind.app.di

import android.content.Context
import com.gastromind.app.BuildConfig
import com.gastromind.app.data.remote.AuthInterceptor
import com.gastromind.app.data.remote.DjangoApi
import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.repository.AuthRepository
import com.gastromind.app.data.repository.AuthRepositoryImpl
import com.gastromind.app.data.repository.CompraRepository
import com.gastromind.app.data.repository.CompraRepositoryImpl
import com.gastromind.app.data.repository.KardexRepository
import com.gastromind.app.data.repository.KardexRepositoryImpl
import com.gastromind.app.data.session.SesionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Inyección manual (sin Hilt), como en el curso: aquí se crean una sola vez el cliente HTTP,
 * la sesión y los repositorios. Cada HU agrega su repositorio aquí (p. ej. pedidoRepository en la HU-09).
 */
class AppContainer(context: Context) {

    // Vive lo que vive la app: guarda la sesión aunque se cierre una pantalla
    private val appScope = CoroutineScope(SupervisorJob())

    val sesionStore = SesionStore(context.applicationContext, appScope)

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sesionStore, appScope))
        .addInterceptor(HttpLoggingInterceptor().apply {
            // Sin el cuerpo: el login lleva la contraseña o el PIN y no deben quedar en el log
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val api: GastroMindApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GastroMindApi::class.java)

    // Mismo cliente HTTP: Django acepta el JWT que emite Spring
    private val djangoApi: DjangoApi = Retrofit.Builder()
        .baseUrl(BuildConfig.DJANGO_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DjangoApi::class.java)

    val authRepository: AuthRepository = AuthRepositoryImpl(api, sesionStore)
    val compraRepository: CompraRepository = CompraRepositoryImpl(api)
    val kardexRepository: KardexRepository = KardexRepositoryImpl(djangoApi)
}
