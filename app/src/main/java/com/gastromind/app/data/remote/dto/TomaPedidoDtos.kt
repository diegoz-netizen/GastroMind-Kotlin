package com.gastromind.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** HU-09: GET /api/mesas de Spring (las mesas del restaurante del token). estado: LIBRE u OCUPADA. */
data class MesaDto(val id: Long, val numero: Int, val estado: String)

/** HU-09: la carta, GET /api/catalogo/productos/?estado=ACTIVO de Django. */
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val precio: BigDecimal,
    @SerializedName("categoria_nombre") val categoriaNombre: String?
)

data class ItemPedidoRequest(val productoId: Long, val cantidad: Int)

/** CA-09: mesa y platillos. Quién lo toma y la fecha los pone el backend. */
data class CrearPedidoRequest(val mesaId: Long, val items: List<ItemPedidoRequest>)

/** HU-09, segunda ronda: se suma al pedido abierto de la mesa. */
data class AgregarPlatillosRequest(val items: List<ItemPedidoRequest>)

/** HU-12 (CA-12): POST /api/pedidos/{id}/cerrar. Solo trae lo servido; los anulados no suman. */
data class CuentaDto(
    val pedidoId: Long,
    val mesaNumero: Int,
    val estado: String,
    val lineas: List<LineaCuentaDto>,
    val total: BigDecimal
)

data class LineaCuentaDto(
    val producto: String,
    val cantidad: Int,
    val precioUnitario: BigDecimal,
    val subtotal: BigDecimal
)
