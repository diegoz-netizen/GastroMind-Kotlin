package com.gastromind.app.ui.mozo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.remote.dto.ItemPedido
import com.gastromind.app.data.remote.dto.PedidoRequest
import com.gastromind.app.data.repository.PedidoRepository
import com.gastromind.app.data.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * HU-09 (CA-09): el mozo elige una mesa libre, agrega platillos de la carta y envía el pedido.
 * Valida lo mismo que la API para avisar antes de enviar; la API sigue siendo la que decide.
 */
class MozoViewModel(private val repo: PedidoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MozoUiState())
    val uiState: StateFlow<MozoUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val mesas = repo.mesas()
            val carta = repo.carta()
            when {
                mesas is Resultado.Error -> _uiState.update { it.copy(cargando = false, errorCarga = mesas.mensaje) }
                carta is Resultado.Error -> _uiState.update { it.copy(cargando = false, errorCarga = carta.mensaje) }
                mesas is Resultado.Exito && carta is Resultado.Exito -> _uiState.update {
                    it.copy(cargando = false, mesas = mesas.valor, carta = carta.valor)
                }
            }
        }
    }

    fun onMesa(id: Long) = _uiState.update { it.copy(mesaId = id, error = null, exito = null) }

    fun onMas(platilloId: Long) = _uiState.update {
        val nueva = it.cantidadDe(platilloId) + 1
        it.copy(cantidades = it.cantidades + (platilloId to nueva), error = null, exito = null)
    }

    fun onMenos(platilloId: Long) = _uiState.update {
        val nueva = it.cantidadDe(platilloId) - 1
        val cantidades = if (nueva > 0) it.cantidades + (platilloId to nueva) else it.cantidades - platilloId
        it.copy(cantidades = cantidades, error = null, exito = null)
    }

    fun enviar() {
        val s = _uiState.value
        if (s.enviando) return
        val mesa = s.mesa
        if (mesa == null) {
            _uiState.update { it.copy(error = "Elige la mesa.", exito = null) }
            return
        }
        if (s.cantidades.isEmpty()) {
            _uiState.update { it.copy(error = "Agrega al menos un platillo.", exito = null) }
            return
        }

        _uiState.update { it.copy(enviando = true, error = null, exito = null) }
        viewModelScope.launch {
            // En el orden de la carta, no en el orden en que se tocaron los botones
            val items = s.carta.mapNotNull { platillo ->
                s.cantidades[platillo.id]?.let { ItemPedido(platillo.id, it) }
            }
            when (val r = repo.crear(PedidoRequest(mesa.id, items))) {
                is Resultado.Exito -> _uiState.update {
                    it.copy(
                        enviando = false,
                        mesaId = null,
                        cantidades = emptyMap(),
                        exito = "Pedido enviado a cocina. La mesa ${r.valor.mesaNumero} quedó ocupada."
                    )
                }
                // La cuenta se conserva: si falló por la mesa, el mozo solo elige otra
                is Resultado.Error -> _uiState.update { it.copy(enviando = false, error = r.mensaje) }
            }
            refrescarMesas()
        }
    }

    // Después de enviar (o de fallar) las mesas pueden haber cambiado: otra persona pudo tomar la misma
    private suspend fun refrescarMesas() {
        val r = repo.mesas()
        if (r is Resultado.Exito) {
            _uiState.update { actual ->
                actual.copy(
                    mesas = r.valor,
                    mesaId = actual.mesaId?.takeIf { id -> r.valor.any { it.id == id && it.libre } }
                )
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                MozoViewModel(app.container.pedidoRepository)
            }
        }
    }
}
