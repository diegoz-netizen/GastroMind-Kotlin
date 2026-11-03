package com.gastromind.app.ui.almacen

import com.gastromind.app.data.remote.dto.InsumoDto
import com.gastromind.app.data.remote.dto.MovimientoKardex
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class KardexUiState(
    val cargandoInsumos: Boolean = true,
    /** No se pudieron traer los insumos: se ofrece reintentar. */
    val errorInsumos: String? = null,
    val insumos: List<InsumoDto> = emptyList(),
    val insumoId: Long? = null,
    val desde: LocalDate? = null,
    val hasta: LocalDate? = null,
    val cargandoMovimientos: Boolean = false,
    val error: String? = null,
    val movimientos: List<MovimientoKardex> = emptyList()
) {
    val insumo: InsumoDto? get() = insumos.firstOrNull { it.id == insumoId }
}

private val FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** La fecha llega en la hora de Perú (2026-10-26T16:13:35-05:00): se muestra ese mismo día. */
fun diaDe(movimiento: MovimientoKardex): String =
    LocalDate.parse(movimiento.fecha.take(10)).format(FORMATO_DIA)

/** Como en el prototipo. Un ajuste sin empleado es el saldo inicial que cargó la migración. */
fun tituloDe(movimiento: MovimientoKardex): String = when (movimiento.tipo) {
    "INGRESO" -> "Ingreso de compra"
    "CONSUMO" -> "Venta"
    "MERMA" -> "Merma"
    "AJUSTE" -> if (movimiento.empleado == null) "Saldo inicial" else "Ajuste"
    else -> movimiento.tipo
}

fun detalleDe(movimiento: MovimientoKardex): String? {
    val origen = movimiento.origen ?: return movimiento.empleado
    return when (movimiento.tipo) {
        "INGRESO" -> listOfNotNull(
            origen.proveedor,
            origen.lote?.let { "lote $it" },
            origen.costoTotal?.let { "S/ " + it.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',') }
        ).joinToString(" · ")
        "CONSUMO" -> listOfNotNull(origen.platillo, origen.pedidoId?.let { "pedido $it" }).joinToString(" · ")
        "MERMA" -> listOfNotNull(origen.motivo?.let(::motivoLegible), movimiento.empleado).joinToString(" · ")
        else -> movimiento.empleado
    }
}

/** El motivo se guarda como MAL_MANEJO: en pantalla va como "Mal manejo". */
fun motivoLegible(motivo: String): String =
    motivo.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

/** Entrada con + y salida con −, como en el prototipo. */
fun entradaConFormato(valor: BigDecimal?): String = valor?.let { "+" + cantidadConFormato(it) }.orEmpty()

fun salidaConFormato(valor: BigDecimal?): String = valor?.let { "−" + cantidadConFormato(it) }.orEmpty()
