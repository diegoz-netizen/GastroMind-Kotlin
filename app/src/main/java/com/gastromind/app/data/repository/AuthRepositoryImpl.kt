package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.AuthResponse
import com.gastromind.app.data.remote.dto.LoginPinRequest
import com.gastromind.app.data.remote.dto.LoginRequest
import com.gastromind.app.data.remote.mensajeDeError
import com.gastromind.app.data.session.EmpleadoRecordado
import com.gastromind.app.data.session.Rol
import com.gastromind.app.data.session.Sesion
import com.gastromind.app.data.session.SesionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val api: GastroMindApi,
    private val sesionStore: SesionStore
) : AuthRepository {

    override val recordado: Flow<EmpleadoRecordado?> = sesionStore.recordado

    override suspend fun entrarConPin(correo: String, pin: String): Resultado<Sesion> =
        entrar { api.loginPin(LoginPinRequest(normalizar(correo), pin)) }

    override suspend fun entrarConCorreo(correo: String, password: String): Resultado<Sesion> =
        entrar { api.login(LoginRequest(normalizar(correo), password)) }

    override suspend fun cerrarSesion() = sesionStore.cerrar()

    override suspend fun olvidarEmpleado() = sesionStore.olvidarEmpleado()

    // El backend busca el correo tal cual y los guarda en minúsculas: "Mozo.Central@…" daría 401 (revisión de Victor)
    private fun normalizar(correo: String) = correo.trim().lowercase()

    private suspend fun entrar(llamada: suspend () -> AuthResponse): Resultado<Sesion> = try {
        val r = llamada()
        val sesion = Sesion(r.token, r.empleadoId, r.nombreCompleto, r.correo, Rol.de(r.rol), r.restauranteId)
        // Al guardarla, la navegación (que observa la sesión) lleva a la pantalla del rol
        sesionStore.guardar(sesion)
        Resultado.Exito(sesion)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }
}
