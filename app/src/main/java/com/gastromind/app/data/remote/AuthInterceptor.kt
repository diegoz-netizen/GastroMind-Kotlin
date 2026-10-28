package com.gastromind.app.data.remote

import com.gastromind.app.data.session.SesionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Pone el JWT en cada petición (Authorization: Bearer …).
 * Si una ruta protegida responde 401 (token vencido o empleado desactivado, HU-03),
 * se cierra la sesión y la app vuelve al login.
 */
class AuthInterceptor(
    private val sesionStore: SesionStore,
    private val scope: CoroutineScope
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val esLogin = original.url.encodedPath.contains("/api/auth/")
        val token = sesionStore.tokenActual()
        val request = if (token != null && !esLogin) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }
        val response = chain.proceed(request)
        if (response.code == 401 && !esLogin && token != null) {
            scope.launch { sesionStore.cerrar() }
        }
        return response
    }
}
