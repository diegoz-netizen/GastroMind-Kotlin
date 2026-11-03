package com.gastromind.app.ui.mozo

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
    val enviado: Boolean = false
) {
    val segundaRonda: Boolean get() = pedidoActual != null
    val totalPlatillos: Int get() = cantidades.values.sum()
    val total: BigDecimal
        get() = carta.fold(BigDecimal.ZERO) { suma, platillo ->
            suma + platillo.precio * BigDecimal(cantidades[platillo.id] ?: 0)
        }

    /** CA-09: un pedido vacío no se envía. */
    val puedeEnviar: Boolean get() = totalPlatillos > 0 && !enviando && !cargando

    /** La carta agrupada como en el prototipo, en el orden en que llega. */
    val cartaPorCategoria: Map<String, List<PlatilloCarta>> get() = carta.groupBy { it.categoria }
}
