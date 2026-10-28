package com.gastromind.app.data.session

/** Roles de la tabla rol del backend. La app tiene una pantalla de inicio por rol operativo. */
enum class Rol { ADMINISTRADOR, MOZO, COCINERO, ALMACENERO, DESCONOCIDO;

    companion object {
        fun de(nombre: String?): Rol = entries.firstOrNull { it.name.equals(nombre, ignoreCase = true) } ?: DESCONOCIDO
    }
}

data class Sesion(
    val token: String,
    val empleadoId: Long,
    val nombreCompleto: String,
    val correo: String,
    val rol: Rol,
    val restauranteId: Long
)

sealed interface EstadoSesion {
    /** Todavía se está leyendo DataStore (dura un instante al abrir la app). */
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val sesion: Sesion) : EstadoSesion
}

/** El último empleado que entró en este dispositivo: la próxima vez solo pide el PIN (HU-27). */
data class EmpleadoRecordado(val correo: String, val nombreCompleto: String)
