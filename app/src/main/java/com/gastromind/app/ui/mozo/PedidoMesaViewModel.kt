package com.gastromind.app.ui.mozo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.data.repository.TomaPedidoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * HU-09 (CA-09): el mozo elige platillos de la carta y los envía a cocina. Si la mesa ya tiene un pedido
 * abierto, lo que envía se suma a ese pedido (segunda ronda) en vez de abrir otro.
 * HU-12 (CA-12): con todo servido, el mozo cierra la mesa y ve la cuenta.
 */
class PedidoMesaViewModel(
    private val mesaId: Long,
    private val repo: TomaPedidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidoMesaUiState())
    val uiState: StateFlow<PedidoMesaUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val carta = async { repo.carta() }
            val pedido = async { repo.pedidoDeMesa(mesaId) }
            val c = carta.await()
            val p = pedido.await()
            _uiState.update {
                when {
                    c is Resultado.Error -> it.copy(cargando = false, errorCarga = c.mensaje)
                    p is Resultado.Error -> it.copy(cargando = false, errorCarga = p.mensaje)
                    else -> it.copy(
                        cargando = false,
                        carta = (c as Resultado.Exito).valor,
                        pedidoActual = (p as Resultado.Exito).valor
                    )
                }
            }
        }
    }

    fun sumar(productoId: Long) = cambiar(productoId, +1)

    fun restar(productoId: Long) = cambiar(productoId, -1)

    private fun cambiar(productoId: Long, delta: Int) {
        _uiState.update {
            val nueva = ((it.cantidades[productoId] ?: 0) + delta).coerceIn(0, MAXIMO_POR_PLATILLO)
            val cantidades = if (nueva == 0) it.cantidades - productoId else it.cantidades + (productoId to nueva)
            it.copy(cantidades = cantidades, errorEnvio = null)
        }
    }

    fun enviar() {
        val estado = _uiState.value
        if (!estado.puedeEnviar) return
        _uiState.update { it.copy(enviando = true, errorEnvio = null) }
        viewModelScope.launch {
            when (val resultado = repo.enviar(mesaId, estado.pedidoActual?.id, estado.itemsEnOrdenDeCarta)) {
                is Resultado.Exito -> _uiState.update {
                    it.copy(enviando = false, enviado = true, pedidoActual = resultado.valor, cantidades = emptyMap())
                }
                // Se queda lo elegido para reintentar sin volver a marcar todo
                is Resultado.Error -> _uiState.update { it.copy(enviando = false, errorEnvio = resultado.mensaje) }
            }
        }
    }

    /** HU-12: primero se confirma, para que un toque sin querer no cierre la mesa. */
    fun pedirCierre() {
        if (_uiState.value.puedeCerrar) _uiState.update { it.copy(confirmandoCierre = true, errorCierre = null) }
    }

    fun cancelarCierre() = _uiState.update { it.copy(confirmandoCierre = false) }

    fun cerrar() {
        val pedido = _uiState.value.pedidoActual ?: return
        if (!_uiState.value.puedeCerrar) return
        _uiState.update { it.copy(confirmandoCierre = false, cerrando = true, errorCierre = null) }
        viewModelScope.launch {
            when (val resultado = repo.cerrar(pedido.id)) {
                is Resultado.Exito -> _uiState.update { it.copy(cerrando = false, cuenta = resultado.valor) }
                is Resultado.Error -> _uiState.update { it.copy(cerrando = false, errorCierre = resultado.mensaje) }
            }
        }
    }

    companion object {
        /** Tope por platillo en una ronda, para que un toque de más no mande 50 a cocina. */
        const val MAXIMO_POR_PLATILLO = 20

        fun factory(mesaId: Long) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                PedidoMesaViewModel(mesaId, app.container.tomaPedidoRepository)
            }
        }
    }
}
