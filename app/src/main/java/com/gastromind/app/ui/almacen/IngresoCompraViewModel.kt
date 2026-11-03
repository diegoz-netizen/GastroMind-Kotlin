package com.gastromind.app.ui.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.remote.dto.IngresoCompraRequest
import com.gastromind.app.data.repository.CompraRepository
import com.gastromind.app.data.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

/**
 * HU-14 (CA-14): el almacenero registra lo que llega del proveedor y el stock sube.
 * Valida lo mismo que la API para avisar antes de enviar; la API sigue siendo la que decide.
 */
class IngresoCompraViewModel(
    private val repo: CompraRepository,
    private val hoy: () -> LocalDate = LocalDate::now
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngresoCompraUiState())
    val uiState: StateFlow<IngresoCompraUiState> = _uiState.asStateFlow()

    init {
        cargarOpciones()
    }

    fun cargarOpciones() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            when (val r = repo.opciones()) {
                is Resultado.Exito -> _uiState.update {
                    it.copy(cargando = false, proveedores = r.valor.proveedores, insumos = r.valor.insumos)
                }
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, errorCarga = r.mensaje) }
            }
        }
    }

    fun onProveedor(id: Long) = _uiState.update { it.copy(proveedorId = id, error = null, exito = null) }

    fun onInsumo(id: Long) = _uiState.update { it.copy(insumoId = id, error = null, exito = null) }

    fun onCantidad(valor: String) = _uiState.update { it.copy(cantidad = valor, error = null, exito = null) }

    fun onCostoTotal(valor: String) = _uiState.update { it.copy(costoTotal = valor, error = null, exito = null) }

    fun onLote(valor: String) = _uiState.update { it.copy(lote = valor, error = null, exito = null) }

    fun onVencimiento(fecha: LocalDate?) = _uiState.update { it.copy(vencimiento = fecha, error = null, exito = null) }

    fun registrar() {
        val s = _uiState.value
        if (s.guardando) return
        val cantidad = leerDecimal(s.cantidad)
        val costo = leerDecimal(s.costoTotal)
        val error = when {
            s.proveedorId == null -> "Elige el proveedor."
            s.insumoId == null -> "Elige el insumo."
            cantidad == null || cantidad <= BigDecimal.ZERO -> "La cantidad debe ser mayor que 0."
            cantidad.stripTrailingZeros().scale() > ESCALA_CANTIDAD -> "La cantidad admite hasta 3 decimales."
            costo == null || costo < BigDecimal.ZERO -> "Escribe el costo total."
            costo.stripTrailingZeros().scale() > ESCALA_COSTO -> "El costo total admite hasta 2 decimales."
            s.lote.trim().length > LARGO_LOTE -> "El lote admite hasta 50 caracteres."
            s.vencimiento?.isBefore(hoy()) == true -> "La fecha de vencimiento ya pasó."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error, exito = null) }
            return
        }

        _uiState.update { it.copy(guardando = true, error = null, exito = null) }
        viewModelScope.launch {
            val request = IngresoCompraRequest(
                proveedorId = s.proveedorId!!,
                ingredienteId = s.insumoId!!,
                cantidad = cantidad!!,
                costoTotal = costo!!,
                lote = s.lote.trim().ifEmpty { null },
                fechaVencimiento = s.vencimiento?.toString()
            )
            when (val r = repo.registrar(request)) {
                is Resultado.Exito -> _uiState.update { actual ->
                    val ingreso = r.valor
                    // Se queda el proveedor (suele traer varios insumos) y el stock se actualiza con el que dio la API
                    actual.copy(
                        guardando = false,
                        insumos = actual.insumos.map {
                            if (it.id == ingreso.ingredienteId) it.copy(stockActual = ingreso.stockResultante) else it
                        },
                        insumoId = null,
                        cantidad = "",
                        costoTotal = "",
                        lote = "",
                        vencimiento = null,
                        exito = "Ingreso registrado. ${ingreso.insumo} quedó en " +
                            "${cantidadConFormato(ingreso.stockResultante)} ${ingreso.unidadMedida}."
                    )
                }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, error = r.mensaje) }
            }
        }
    }

    companion object {
        private const val ESCALA_CANTIDAD = 3
        private const val ESCALA_COSTO = 2
        private const val LARGO_LOTE = 50

        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                IngresoCompraViewModel(app.container.compraRepository)
            }
        }
    }
}
