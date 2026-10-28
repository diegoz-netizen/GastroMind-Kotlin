package com.gastromind.app.data.repository

import com.gastromind.app.data.session.EmpleadoRecordado
import com.gastromind.app.data.session.Sesion
import kotlinx.coroutines.flow.Flow

/** Única puerta del login para los ViewModels (interface: en los tests se usa una falsa). */
interface AuthRepository {
    val recordado: Flow<EmpleadoRecordado?>
    suspend fun entrarConPin(correo: String, pin: String): Resultado<Sesion>
    suspend fun entrarConCorreo(correo: String, password: String): Resultado<Sesion>
    suspend fun cerrarSesion()
    suspend fun olvidarEmpleado()
}
