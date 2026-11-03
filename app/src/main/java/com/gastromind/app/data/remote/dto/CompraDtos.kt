package com.gastromind.app.data.remote.dto

import java.math.BigDecimal

/** HU-14: lo que necesita el formulario (GET /api/compras/opciones). */
data class CompraOpcionesResponse(
    val proveedores: List<ProveedorOpcion>,
    val insumos: List<InsumoOpcion>
)

data class ProveedorOpcion(val id: Long, val nombre: String)

data class InsumoOpcion(val id: Long, val nombre: String, val unidadMedida: String, val stockActual: BigDecimal)

/** HU-14: lo que llega al almacén. El restaurante y quién lo registra salen del token. */
data class IngresoCompraRequest(
    val proveedorId: Long,
    val ingredienteId: Long,
    val cantidad: BigDecimal,
    val costoTotal: BigDecimal,
    val lote: String?,
    /** AAAA-MM-DD; null si el insumo no es perecible. */
    val fechaVencimiento: String?
)

/** Solo lo que usa la pantalla de la respuesta de POST /api/compras. */
data class IngresoCompraResponse(
    val id: Long,
    val ingredienteId: Long,
    val insumo: String,
    val unidadMedida: String,
    val stockResultante: BigDecimal
)
