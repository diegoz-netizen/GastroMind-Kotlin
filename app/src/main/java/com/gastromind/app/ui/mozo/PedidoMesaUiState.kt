package com.gastromind.app.ui.mozo

import com.gastromind.app.data.repository.Cuenta
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.repository.Pedido
import com.gastromind.app.data.repository.PlatilloCarta
import java.math.BigDecimal

data class PedidoMesaUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val carta: List<PlatilloCarta> = emptyList(),
    /** El pedido abierto de la mesa: si existe, lo que se envía es una segunda ronda. */
    val pedidoActual: Pedido? = null,
    /** productoId → cantidad de lo que se está pidiendo ahora. */
    val cantidades: Map<Long, Int> = emptyMap(),
    val enviando: Boolean = false,
    val errorEnvio: String? = null,
    val enviado: Boolean = false,
    /** HU-12: se pidió confirmar el cierre de la mesa. */
    val confirmandoCierre: Boolean = false,
    val cerrando: Boolean = false,
    val errorCierre: String? = null,
    /** La cuenta que devolvió el backend: la mesa ya está cerrada y libre. */
    val cuenta: Cuenta? = null
) {
    val segundaRonda: Boolean get() = pedidoActual != null
    val totalPlatillos: Int get() = cantidades.values.sum()
    val total: BigDecimal
        get() = carta.fold(BigDecimal.ZERO) { suma, platillo ->
            suma + platillo.precio * BigDecimal(cantidades[platillo.id] ?: 0)
        }

    /** CA-09: un pedido vacío no se envía. */
    val puedeEnviar: Boolean get() = totalPlatillos > 0 && !enviando && !cargando

    /** CA-12: se cierra cuando todo lo de la mesa está servido o anulado, y no hay una ronda a medio elegir. */
    val todoServido: Boolean
        get() = pedidoActual?.platillos?.all { it.estado == EstadoPlatillo.SERVIDO || it.estado == EstadoPlatillo.ANULADO } == true

    val puedeCerrar: Boolean get() = todoServido && cantidades.isEmpty() && !cerrando && !enviando

    /** La carta agrupada como en el prototipo, en el orden en que llega. */
    val cartaPorCategoria: Map<String, List<PlatilloCarta>> get() = carta.groupBy { it.categoria }
}
