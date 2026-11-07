package com.gastromind.app.data.remote.dto

import java.math.BigDecimal

/** HU-30: lo que el almacenero contó. Quién cuenta, el restaurante y la fecha los pone el backend. */
data class ConteoRequest(val ingredienteId: Long, val cantidad: BigDecimal)

/** POST /api/conteos de Spring: diferencia = contado − sistema (negativo si falta). */
data class ConteoResponse(
    val id: Long,
    val ingredienteId: Long,
    val insumo: String,
    val unidadMedida: String,
    val cantidadContada: BigDecimal,
    val stockSistema: BigDecimal,
    val diferencia: BigDecimal
)
