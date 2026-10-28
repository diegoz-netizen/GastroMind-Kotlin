package com.gastromind.app.data.remote

import com.gastromind.app.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

private val gson = Gson()

/**
 * Convierte un error de la API en un texto para la pantalla. Usa el mensaje del backend (HL-014)
 * y, si no viene, uno según el código.
 */
fun mensajeDeError(e: Throwable): String = when (e) {
    is HttpException -> mensajeDeError(e.code(), e.response()?.errorBody()?.string())
    is IOException -> "No se pudo conectar con el servidor. Revisa tu conexión."
    else -> "Ocurrió un error inesperado."
}

fun mensajeDeError(codigo: Int, cuerpo: String?): String {
    val error = runCatching { gson.fromJson(cuerpo, ErrorResponse::class.java) }.getOrNull()
    // En una validación (400) el detalle útil está en el primer campo
    error?.campos?.values?.firstOrNull()?.let { return it }
    error?.mensaje?.takeIf { it.isNotBlank() }?.let { return it }
    return when (codigo) {
        401 -> "Credenciales inválidas"
        403 -> "No tienes permiso para esta acción."
        429 -> "Demasiados intentos. Espera unos minutos."
        in 500..599 -> "El servidor tuvo un problema. Intenta de nuevo."
        else -> "Ocurrió un error ($codigo)."
    }
}
