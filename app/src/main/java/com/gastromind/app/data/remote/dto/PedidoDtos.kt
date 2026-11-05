package com.gastromind.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** HU-09: mesa del salón (GET /api/mesas de Spring). El estado es LIBRE u OCUPADA. */
data class MesaDto(val id: Long, val numero: Int, val estado: String) {
    val libre: Boolean get() = estado == "LIBRE"
}

/** HU-09: platillo de la carta (GET /api/catalogo/productos/?estado=ACTIVO de Django). Solo lo que usa la pantalla. */
data class PlatilloCarta(
    val id: Long,
    val nombre: String,
    val precio: BigDecimal,
    @SerializedName("categoria_nombre") val categoriaNombre: String? = null
)

/** HU-09 (CA-09): mesa y platillos. El restaurante y quién toma el pedido salen del token. */
data class PedidoRequest(val mesaId: Long, val items: List<ItemPedido>)

data class ItemPedido(val productoId: Long, val cantidad: Int)

/** Solo lo que usa la pantalla de la respuesta de POST /api/pedidos. */
data class PedidoCreado(val id: Long, val mesaId: Long, val mesaNumero: Int, val estado: String)
