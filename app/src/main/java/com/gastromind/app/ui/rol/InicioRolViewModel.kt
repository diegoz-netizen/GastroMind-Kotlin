package com.gastromind.app.ui.rol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

/** Lo común de las pantallas de inicio de cada rol. Por ahora, solo cerrar sesión. */
class InicioRolViewModel(private val repo: AuthRepository) : ViewModel() {

    fun cerrarSesion() {
        viewModelScope.launch { repo.cerrarSesion() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                InicioRolViewModel(app.container.authRepository)
            }
        }
    }
}
