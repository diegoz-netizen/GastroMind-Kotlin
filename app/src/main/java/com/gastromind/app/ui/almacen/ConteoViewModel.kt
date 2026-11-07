package com.gastromind.app.ui.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.remote.dto.ConteoRequest
import com.gastromind.app.data.remote.dto.ConteoResponse
import com.gastromind.app.data.remote.dto.InsumoOpcion
import com.gastromind.app.data.repository.CompraRepository
import com.gastromind.app.data.repository.ConteoRepository
import com.gastromind.app.data.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class ConteoUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val insumos: List<InsumoOpcion> = emptyList(),
    val insumoId: Long? = null,
    val cantidad: String = "",
    val guardando: Boolean = false,
    val error: String? = null,
    /** El último conteo registrado, con su diferencia contra el sistema. */
    val ultimo: ConteoResponse? = null
) {
    val insumo: InsumoOpcion? get() = insumos.firstOrNull { it.id == insumoId }
}

/**
 * HU-30 (CA-30): el almacenero registra lo que contó de un insumo y ve la diferencia con el sistema.
 * El conteo no cambia el stock; la diferencia queda para el reporte teórico contra real (HU-20).
 */
class ConteoViewModel(
    private val compras: CompraRepository,
    private val repo: ConteoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConteoUiState())
    val uiState: StateFlow<ConteoUiState> = _uiState.asStateFlow()

    init {
        actualizar()
    }

    /** Los insumos con su stock de ahora: se vuelve a leer al abrir la pestaña. */
    fun actualizar() {
        viewModelScope.launch {
            when (val r = compras.opciones()) {
                is Resultado.Exito -> _uiState.update { it.copy(cargando = false, errorCarga = null, insumos = r.valor.insumos) }
                // Si ya había insumos a la vista, se quedan
                is Resultado.Error -> _uiState.update {
                    it.copy(cargando = false, errorCarga = if (it.insumos.isEmpty()) r.mensaje else null)
                }
            }
        }
    }

    fun onInsumo(id: Long) = _uiState.update { it.copy(insumoId = id, error = null) }

    fun onCantidad(valor: String) = _uiState.update { it.copy(cantidad = valor, error = null) }

    fun registrar() {
        val s = _uiState.value
        if (s.guardando) return
        val cantidad = leerDecimal(s.cantidad)
        val error = when {
            s.insumoId == null -> "Elige el insumo."
            cantidad == null -> "Escribe la cantidad contada."
            cantidad < BigDecimal.ZERO -> "La cantidad contada no puede ser negativa."
            cantidad.stripTrailingZeros().scale() > ESCALA_CANTIDAD -> "La cantidad admite hasta 3 decimales."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.registrar(ConteoRequest(s.insumoId!!, cantidad!!))) {
                is Resultado.Exito -> _uiState.update { it.copy(guardando = false, cantidad = "", ultimo = r.valor) }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, error = r.mensaje) }
            }
        }
    }

    companion object {
        private const val ESCALA_CANTIDAD = 3

        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                ConteoViewModel(app.container.compraRepository, app.container.conteoRepository)
            }
        }
    }
}
