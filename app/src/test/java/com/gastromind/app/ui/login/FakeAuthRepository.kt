package com.gastromind.app.ui.login

import com.gastromind.app.data.repository.AuthRepository
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.data.session.EmpleadoRecordado
import com.gastromind.app.data.session.Rol
import com.gastromind.app.data.session.Sesion
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(recordado: EmpleadoRecordado? = null) : AuthRepository {
    override val recordado = MutableStateFlow(recordado)
    var respuesta: Resultado<Sesion> = Resultado.Exito(Sesion("jwt", 1, "Ana Mozo", "ana@test.com", Rol.MOZO, 1))
    val intentosPin = mutableListOf<Pair<String, String>>()
    val intentosCorreo = mutableListOf<Pair<String, String>>()

    override suspend fun entrarConPin(correo: String, pin: String): Resultado<Sesion> {
        intentosPin += correo to pin
        return respuesta
    }

    override suspend fun entrarConCorreo(correo: String, password: String): Resultado<Sesion> {
        intentosCorreo += correo to password
        return respuesta
    }

    override suspend fun cerrarSesion() {}

    override suspend fun olvidarEmpleado() {
        recordado.value = null
    }
}
