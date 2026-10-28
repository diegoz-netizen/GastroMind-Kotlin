package com.gastromind.app.data.remote.dto

data class LoginRequest(val correo: String, val password: String)

data class LoginPinRequest(val correo: String, val pin: String)

/** Lo que responde Spring en /api/auth/login y /api/auth/login-pin (AuthResponse). */
data class AuthResponse(
    val token: String,
    val empleadoId: Long,
    val nombreCompleto: String,
    val correo: String,
    val rol: String,
    val restauranteId: Long
)

/** Formato común de errores del backend (HL-014): {error, mensaje, campos?}. */
data class ErrorResponse(
    val error: String?,
    val mensaje: String?,
    val campos: Map<String, String>?
)
