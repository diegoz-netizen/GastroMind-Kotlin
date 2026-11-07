package com.gastromind.app.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.repository.Pedido
import com.gastromind.app.data.repository.PedidoRepository
import com.gastromind.app.data.repository.Resultado
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/** CA-10: una comanda nueva aparece en menos de 5 s. */
const val INTERVALO_REFRESCO_MS = 3_000L

/** Lo que el cocinero puede hacer con un platillo: empezarlo y terminarlo. */
val pasoDeCocina: (EstadoPlatillo) -> EstadoPlatillo? = {
    when (it) {
        EstadoPlatillo.PENDIENTE -> EstadoPlatillo.EN_PREPARACION
        EstadoPlatillo.EN_PREPARACION -> EstadoPlatillo.LISTO
        else -> null
    }
}

/** Lo que el mozo puede hacer con un platillo: llevarlo a la mesa cuando está listo. */
val pasoDeMozo: (EstadoPlatillo) -> EstadoPlatillo? = {
    if (it == EstadoPlatillo.LISTO) EstadoPlatillo.SERVIDO else null
}

/**
 * Los pedidos en curso, al día: lo comparten la comanda de cocina (HU-10, HU-11) y la vista del mozo (HU-11).
 * Solo cambia qué paso puede dar cada rol; el backend vuelve a validarlo.
 */
class PedidosEnVivoViewModel(
    private val repo: PedidoRepository,
    private val paso: (EstadoPlatillo) -> EstadoPlatillo?,
    private val ahora: () -> LocalDateTime = LocalDateTime::now
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    private var pedidos = emptyList<Pedido>()
    private var cambiosTerminados = 0

    /** Corre mientras la pantalla está a la vista; la pantalla lo cancela al salir. */
    suspend fun mantenerActualizado() {
        while (true) {
            refrescar()
            delay(INTERVALO_REFRESCO_MS)
        }
    }

    fun avanzar(pedidoId: Long, platillo: PlatilloComanda) {
        val nuevo = platillo.siguiente ?: return
        if (platillo.id in _uiState.value.cambiando) return
        _uiState.update { it.copy(cambiando = it.cambiando + platillo.id, errorCambio = null) }
        viewModelScope.launch {
            val resultado = repo.cambiarEstado(pedidoId, platillo.id, nuevo)
            cambiosTerminados++
            _uiState.update { it.copy(cambiando = it.cambiando - platillo.id) }
            when (resultado) {
                is Resultado.Exito -> {
                    pedidos = pedidos.map { if (it.id == pedidoId) resultado.valor else it }
                    publicar()
                }
                is Resultado.Error -> {
                    // P. ej. otro dispositivo ya lo cambió (409): se muestra el aviso y lo que hay ahora
                    _uiState.update { it.copy(errorCambio = resultado.mensaje) }
                    refrescar()
                }
            }
        }
    }

    /** HU-18: otra acción (anular) ya trajo el pedido actualizado; se muestra sin esperar el refresco. */
    fun reemplazar(pedido: Pedido) {
        cambiosTerminados++
        pedidos = pedidos.map { if (it.id == pedido.id) pedido else it }
        publicar()
    }

    private suspend fun refrescar() {
        val cambiosAlEmpezar = cambiosTerminados
        val resultado = repo.vigentes()
        // Se leyó antes de que terminara un cambio: traería el estado viejo y el botón "retrocedería"
        if (cambiosAlEmpezar != cambiosTerminados) return
        when (resultado) {
            is Resultado.Exito -> {
                pedidos = resultado.valor
                _uiState.update { it.copy(errorLectura = null) }
            }
            // Se quedan los últimos pedidos a la vista: en cocina es mejor eso que una pantalla vacía
            is Resultado.Error -> _uiState.update { it.copy(errorLectura = resultado.mensaje) }
        }
        publicar()
    }

    private fun publicar() {
        val momento = ahora()
        _uiState.update {
            it.copy(cargando = false, ahora = momento, comandas = pedidos.mapNotNull { pedido -> comandaDe(pedido, momento, paso) })
        }
    }

    companion object {
        fun factory(paso: (EstadoPlatillo) -> EstadoPlatillo?) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                PedidosEnVivoViewModel(app.container.pedidoRepository, paso)
            }
        }
    }
}

private val ESTADOS_EN_CURSO = setOf(EstadoPlatillo.PENDIENTE, EstadoPlatillo.EN_PREPARACION, EstadoPlatillo.LISTO)

// Los servidos y anulados ya no son trabajo de nadie; un pedido sin platillos en curso sale de la pantalla
private fun comandaDe(pedido: Pedido, momento: LocalDateTime, paso: (EstadoPlatillo) -> EstadoPlatillo?): Comanda? {
    val platillos = pedido.platillos.filter { it.estado in ESTADOS_EN_CURSO }
        .map { PlatilloComanda(it.id, it.nombre, it.cantidad, it.estado, paso(it.estado)) }
    if (platillos.isEmpty()) return null
    val minutos = ChronoUnit.MINUTES.between(pedido.fechaRegistro, momento).coerceAtLeast(0)
    return Comanda(pedido.id, pedido.mesaNumero, pedido.fechaRegistro, minutos, urgenciaDe(minutos), platillos)
}

private fun urgenciaDe(minutos: Long) = when {
    minutos < MINUTOS_NUEVA -> Urgencia.NUEVA
    minutos >= MINUTOS_DEMORADA -> Urgencia.DEMORADA
    else -> Urgencia.NORMAL
}

private const val MINUTOS_NUEVA = 2L
private const val MINUTOS_DEMORADA = 10L
