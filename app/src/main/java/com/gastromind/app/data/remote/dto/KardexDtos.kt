package com.gastromind.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** Insumo del restaurante (GET /api/catalogo/insumos/ de Django). */
data class InsumoDto(
    val id: Long,
    val nombre: String,
    @SerializedName("unidad_medida") val unidadMedida: String,
    @SerializedName("stock_actual") val stockActual: BigDecimal
)

/** HU-15: GET /api/catalogo/insumos/{id}/kardex/ de Django. */
data class KardexResponse(
    val insumo: InsumoDto,
    val movimientos: List<MovimientoKardex>
)

data class MovimientoKardex(
    val id: Long,
    /** Fecha y hora con su zona, en la hora de Perú. */
    val fecha: String,
    /** INGRESO, CONSUMO, MERMA o AJUSTE. */
    val tipo: String,
    val entrada: BigDecimal?,
    val salida: BigDecimal?,
    val saldo: BigDecimal,
    val empleado: String?,
    val origen: OrigenKardex?
)

/** De dónde salió: compra (proveedor, lote, costo), merma (motivo) o consumo (pedido, platillo). */
data class OrigenKardex(
    val proveedor: String? = null,
    val lote: String? = null,
    @SerializedName("costo_total") val costoTotal: BigDecimal? = null,
    val motivo: String? = null,
    @SerializedName("pedido_id") val pedidoId: Long? = null,
    val platillo: String? = null
)
