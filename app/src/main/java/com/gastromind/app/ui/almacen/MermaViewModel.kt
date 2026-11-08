package com.gastromind.app.ui.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.remote.dto.InsumoOpcion
import com.gastromind.app.data.remote.dto.MermaRequest
import com.gastromind.app.data.remote.dto.MermaResponse
import com.gastromind.app.data.remote.dto.MotivoMermaDto
import com.gastromind.app.data.repository.CompraRepository
import com.gastromind.app.data.repository.MermaRepository
import com.gastromind.app.data.repository.Resultado
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class MermaUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val insumos: List<InsumoOpcion> = emptyList(),
    val motivos: List<MotivoMermaDto> = emptyList(),
    val insumoId: Long? = null,
    val cantidad: String = "",
    val motivoId: Long? = null,
    val guardando: Boolean = false,
    val error: String? = null,
    /** La última merma registrada, con el stock que quedó y lo que costó. */
    val ultima: MermaResponse? = null
) {
    val insumo: InsumoOpcion? get() = insumos.firstOrNull { it.id == insumoId }
}

/**
 * HU-17 (CA-17): el almacenero registra una merma con insumo, cantidad y motivo (obligatorio).
 * El backend descuenta el stock y deja el movimiento MERMA a su nombre.
 */
class MermaViewModel(
    private val compras: CompraRepository,
    private val repo: MermaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MermaUiState())
    val uiState: StateFlow<MermaUiState> = _uiState.asStateFlow()

    init {
        actualizar()
    }

    /** Insumos con su stock de ahora y los motivos: se vuelve a leer al abrir la pestaña. */
    fun actualizar() {
        viewModelScope.launch {
            val insumos = async { compras.opciones() }
            val motivos = async { repo.motivos() }
            val ri = insumos.await()
            val rm = motivos.await()
            _uiState.update { s ->
                val error = (ri as? Resultado.Error)?.mensaje ?: (rm as? Resultado.Error)?.mensaje
                s.copy(
                    cargando = false,
                    insumos = (ri as? Resultado.Exito)?.valor?.insumos ?: s.insumos,
                    // En el almacén no se ofrece PLATO_ANULADO: ese lo registra la cocina al anular (HU-18)
                    motivos = (rm as? Resultado.Exito)?.valor?.filterNot { it.nombre == MOTIVO_DE_COCINA } ?: s.motivos,
                    // Si ya había datos a la vista, se quedan
                    errorCarga = if (s.insumos.isEmpty() || s.motivos.isEmpty()) error else null
                )
            }
        }
    }

    fun onInsumo(id: Long) = _uiState.update { it.copy(insumoId = id, error = null) }

    fun onCantidad(valor: String) = _uiState.update { it.copy(cantidad = valor, error = null) }

    fun onMotivo(id: Long) = _uiState.update { it.copy(motivoId = id, error = null) }

    fun registrar() {
        val s = _uiState.value
        if (s.guardando) return
        val cantidad = leerDecimal(s.cantidad)
        val error = when {
            s.insumoId == null -> "Elige el insumo."
            cantidad == null -> "Escribe la cantidad perdida."
            cantidad <= BigDecimal.ZERO -> "La cantidad debe ser mayor que 0."
            cantidad.stripTrailingZeros().scale() > ESCALA_CANTIDAD -> "La cantidad admite hasta 3 decimales."
            s.motivoId == null -> "Elige el motivo."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.registrar(MermaRequest(s.insumoId!!, cantidad!!, s.motivoId!!))) {
                is Resultado.Exito -> {
                    _uiState.update { it.copy(guardando = false, cantidad = "", motivoId = null, ultima = r.valor) }
                    // El stock del insumo cambió: se relee para la siguiente merma
                    actualizar()
                }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, error = r.mensaje) }
            }
        }
    }

    companion object {
        private const val ESCALA_CANTIDAD = 3
        const val MOTIVO_DE_COCINA = "PLATO_ANULADO"

        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                MermaViewModel(app.container.compraRepository, app.container.mermaRepository)
            }
        }
    }
}
