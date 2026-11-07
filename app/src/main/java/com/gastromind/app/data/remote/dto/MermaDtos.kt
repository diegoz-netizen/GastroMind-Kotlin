package com.gastromind.app.data.remote.dto

import java.math.BigDecimal

/** HU-17: la merma que registra el almacenero. El responsable y el restaurante los pone el backend. */
data class MermaRequest(val ingredienteId: Long, val cantidad: BigDecimal, val motivoId: Long)

/** POST /api/mermas de Spring: la merma con el stock que quedó y lo que costó. */
data class MermaResponse(
    val id: Long,
    val insumo: String,
    val cantidad: BigDecimal,
    val unidadMedida: String,
    val motivo: String,
    val stockDespues: BigDecimal,
    val costo: BigDecimal,
    val registradoPor: String
)
