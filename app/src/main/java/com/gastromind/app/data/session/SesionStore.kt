package com.gastromind.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.dataStore by preferencesDataStore(name = "sesion")

/**
 * Sesión del empleado en DataStore: el token y sus datos. Nunca se guarda la contraseña ni el PIN.
 * Al cerrar sesión se borra el token, pero se recuerda el correo y el nombre para entrar solo con el PIN.
 */
class SesionStore(context: Context, scope: CoroutineScope) {

    private val dataStore = context.dataStore

    val estado: StateFlow<EstadoSesion> = dataStore.data
        .map { it.aSesion()?.let(EstadoSesion::Activa) ?: EstadoSesion.SinSesion }
        .stateIn(scope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    val recordado: Flow<EmpleadoRecordado?> = dataStore.data.map { p ->
        val correo = p[CORREO_RECORDADO] ?: return@map null
        EmpleadoRecordado(correo, p[NOMBRE_RECORDADO].orEmpty())
    }

    /** Para el interceptor de OkHttp, que no puede suspender. */
    fun tokenActual(): String? = (estado.value as? EstadoSesion.Activa)?.sesion?.token

    suspend fun guardar(sesion: Sesion) {
        dataStore.edit {
            it[TOKEN] = sesion.token
            it[EMPLEADO_ID] = sesion.empleadoId
            it[NOMBRE] = sesion.nombreCompleto
            it[CORREO] = sesion.correo
            it[ROL] = sesion.rol.name
            it[RESTAURANTE_ID] = sesion.restauranteId
            it[CORREO_RECORDADO] = sesion.correo
            it[NOMBRE_RECORDADO] = sesion.nombreCompleto
        }
    }

    suspend fun cerrar() {
        dataStore.edit { p -> listOf(TOKEN, EMPLEADO_ID, NOMBRE, CORREO, ROL, RESTAURANTE_ID).forEach { p.remove(it) } }
    }

    /** "¿No eres tú?": otro empleado va a usar este dispositivo. */
    suspend fun olvidarEmpleado() {
        dataStore.edit { it.remove(CORREO_RECORDADO); it.remove(NOMBRE_RECORDADO) }
    }

    private fun Preferences.aSesion(): Sesion? {
        val token = this[TOKEN] ?: return null
        return Sesion(
            token = token,
            empleadoId = this[EMPLEADO_ID] ?: 0,
            nombreCompleto = this[NOMBRE].orEmpty(),
            correo = this[CORREO].orEmpty(),
            rol = Rol.de(this[ROL]),
            restauranteId = this[RESTAURANTE_ID] ?: 0
        )
    }

    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val EMPLEADO_ID = longPreferencesKey("empleado_id")
        val NOMBRE = stringPreferencesKey("nombre_completo")
        val CORREO = stringPreferencesKey("correo")
        val ROL = stringPreferencesKey("rol")
        val RESTAURANTE_ID = longPreferencesKey("restaurante_id")
        val CORREO_RECORDADO = stringPreferencesKey("correo_recordado")
        val NOMBRE_RECORDADO = stringPreferencesKey("nombre_recordado")
    }
}
