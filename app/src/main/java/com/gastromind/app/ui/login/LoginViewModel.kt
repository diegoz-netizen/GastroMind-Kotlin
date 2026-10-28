package com.gastromind.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.repository.AuthRepository
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.ui.components.LARGO_PIN
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * HU-27 (PIN) y HU-02 (correo y contraseña). Al entrar bien no navega: el repositorio guarda la sesión
 * y GastroMindNavHost, que la observa, lleva a la pantalla del rol.
 */
class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.recordado.collect { r -> _uiState.update { it.copy(recordado = r) } }
        }
    }

    fun onCorreo(valor: String) = _uiState.update { it.copy(correo = valor.trim(), error = null) }

    fun onPassword(valor: String) = _uiState.update { it.copy(password = valor, error = null) }

    fun onModo(modo: ModoIngreso) = _uiState.update { it.copy(modo = modo, pin = "", password = "", error = null) }

    fun onDigito(d: Char) {
        val s = _uiState.value
        if (s.cargando || !d.isDigit() || s.pin.length >= LARGO_PIN) return
        val pin = s.pin + d
        _uiState.update { it.copy(pin = pin, error = null) }
        // Con el cuarto dígito entra solo, sin botón
        if (pin.length == LARGO_PIN) entrarConPin()
    }

    fun onBorrar() = _uiState.update { it.copy(pin = it.pin.dropLast(1), error = null) }

    /** "¿No eres tú?": se olvida al último empleado y se pide el correo. */
    fun onNoSoyYo() = viewModelScope.launch {
        repo.olvidarEmpleado()
        _uiState.update { it.copy(pin = "", correo = "", error = null) }
    }

    private fun entrarConPin() {
        val s = _uiState.value
        if (!correoValido(s.correoParaPin)) {
            _uiState.update { it.copy(pin = "", error = "Escribe tu correo antes del PIN.") }
            return
        }
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            when (val r = repo.entrarConPin(s.correoParaPin, s.pin)) {
                is Resultado.Exito -> _uiState.update { it.copy(cargando = false, pin = "") }
                // El PIN se borra para volver a escribirlo; el mensaje no dice qué dato falló (lo decide el backend)
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, pin = "", error = r.mensaje) }
            }
        }
    }

    fun entrarConCorreo() {
        val s = _uiState.value
        val error = when {
            !correoValido(s.correo) -> "Escribe un correo válido."
            s.password.isBlank() -> "Escribe tu contraseña."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.entrarConCorreo(s.correo, s.password)) {
                is Resultado.Exito -> _uiState.update { it.copy(cargando = false, password = "") }
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    private fun correoValido(correo: String) = correo.contains('@') && correo.substringAfter('@').contains('.')

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}
