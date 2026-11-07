package com.gastromind.app.ui.mozo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.repository.Mesa
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.data.repository.TomaPedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MesasUiState(
    val cargando: Boolean = true,
    val mesas: List<Mesa> = emptyList(),
    val error: String? = null
)

/** HU-09: el salón. Se vuelve a leer cada vez que la pantalla aparece, para ver las mesas que se ocuparon. */
class MesasViewModel(private val repo: TomaPedidoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MesasUiState())
    val uiState: StateFlow<MesasUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            when (val resultado = repo.mesas()) {
                is Resultado.Exito -> _uiState.update { MesasUiState(cargando = false, mesas = resultado.valor) }
                // Si ya había mesas a la vista, se quedan: mejor eso que una pantalla vacía
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                MesasViewModel(app.container.tomaPedidoRepository)
            }
        }
    }
}
