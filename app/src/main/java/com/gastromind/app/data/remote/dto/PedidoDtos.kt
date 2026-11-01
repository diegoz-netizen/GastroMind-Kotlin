package com.gastromind.app.data.remote.dto

/** Lo que responde Spring en /api/pedidos (PedidoResponse). fechaRegistro viene en UTC, sin zona: el backend corre en UTC. */
data class PedidoResponse(
    val id: Long,
    val mesaNumero: Int,
    val fechaRegistro: String,
    val detalles: List<DetalleResponse>
)

data class DetalleResponse(
    val id: Long,
    val producto: String,
    val cantidad: Int,
    val estado: String
)

data class CambioEstadoRequest(val estado: String)
