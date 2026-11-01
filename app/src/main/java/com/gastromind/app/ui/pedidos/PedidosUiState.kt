package com.gastromind.app.ui.pedidos

import com.gastromind.app.data.repository.EstadoPlatillo
import java.time.LocalDateTime

enum class Urgencia { NUEVA, NORMAL, DEMORADA }

/** siguiente es null si este rol no puede avanzar el platillo desde su estado. */
data class PlatilloComanda(
    val id: Long,
    val nombre: String,
    val cantidad: Int,
    val estado: EstadoPlatillo,
    val siguiente: EstadoPlatillo?
)

data class Comanda(
    val pedidoId: Long,
    val mesaNumero: Int,
    val horaIngreso: LocalDateTime,
    val minutos: Long,
    val urgencia: Urgencia,
    val platillos: List<PlatilloComanda>
)

data class PedidosUiState(
    val cargando: Boolean = true,
    val ahora: LocalDateTime? = null,
    val comandas: List<Comanda> = emptyList(),
    val cambiando: Set<Long> = emptySet(),
    val errorLectura: String? = null,
    val errorCambio: String? = null
) {
    val totalPlatillos: Int get() = comandas.sumOf { it.platillos.size }
    /** El cambio que falló se muestra primero: lo pidió el usuario y hay que decirle que no se hizo. */
    val error: String? get() = errorCambio ?: errorLectura
}
