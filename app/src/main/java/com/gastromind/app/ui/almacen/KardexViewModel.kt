package com.gastromind.app.ui.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.repository.KardexRepository
import com.gastromind.app.data.repository.Resultado
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** HU-15 (CA-15): ingresos, salidas por venta y mermas de un insumo, filtrables por fecha. */
class KardexViewModel(private val repo: KardexRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(KardexUiState())
    val uiState: StateFlow<KardexUiState> = _uiState.asStateFlow()

    // Si cambian el filtro rápido, solo vale la última consulta
    private var consulta: Job? = null

    /** Al abrir la pestaña: una compra recién registrada ya tiene que verse. */
    fun actualizar() {
        _uiState.update { it.copy(cargandoInsumos = it.insumos.isEmpty(), errorInsumos = null) }
        viewModelScope.launch {
            when (val r = repo.insumos()) {
                is Resultado.Exito -> {
                    _uiState.update { it.copy(cargandoInsumos = false, insumos = r.valor) }
                    consultar()
                }
                is Resultado.Error -> _uiState.update { it.copy(cargandoInsumos = false, errorInsumos = r.mensaje) }
            }
        }
    }

    fun onInsumo(id: Long) {
        _uiState.update { it.copy(insumoId = id, movimientos = emptyList()) }
        consultar()
    }

    fun onDesde(fecha: LocalDate?) {
        _uiState.update { it.copy(desde = fecha) }
        consultar()
    }

    fun onHasta(fecha: LocalDate?) {
        _uiState.update { it.copy(hasta = fecha) }
        consultar()
    }

    private fun consultar() {
        consulta?.cancel()
        val s = _uiState.value
        val insumoId = s.insumoId ?: return
        if (s.desde != null && s.hasta != null && s.desde > s.hasta) {
            _uiState.update {
                it.copy(cargandoMovimientos = false, movimientos = emptyList(),
                    error = "La fecha inicial no puede ser mayor que la final.")
            }
            return
        }
        _uiState.update { it.copy(cargandoMovimientos = true, error = null) }
        consulta = viewModelScope.launch {
            when (val r = repo.kardex(insumoId, s.desde, s.hasta)) {
                is Resultado.Exito -> _uiState.update {
                    it.copy(cargandoMovimientos = false, movimientos = r.valor.movimientos)
                }
                is Resultado.Error -> _uiState.update {
                    it.copy(cargandoMovimientos = false, movimientos = emptyList(), error = r.mensaje)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                KardexViewModel(app.container.kardexRepository)
            }
        }
    }
}
