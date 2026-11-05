package com.gastromind.app.ui.mozo

import com.gastromind.app.data.remote.dto.MesaDto
import com.gastromind.app.data.remote.dto.PlatilloCarta
import java.math.BigDecimal
import java.math.RoundingMode

data class MozoUiState(
    val cargando: Boolean = true,
    /** No se pudieron traer las mesas o la carta: se ofrece reintentar. */
    val errorCarga: String? = null,
    val mesas: List<MesaDto> = emptyList(),
    val carta: List<PlatilloCarta> = emptyList(),
    val mesaId: Long? = null,
    /** Platillo -> cantidad. Solo están los que el mozo agregó, siempre con cantidad mayor que 0. */
    val cantidades: Map<Long, Int> = emptyMap(),
    val enviando: Boolean = false,
    val error: String? = null,
    val exito: String? = null
) {
    /** Solo una mesa libre puede recibir un pedido nuevo. */
    val mesasLibres: List<MesaDto> get() = mesas.filter { it.libre }
    val mesa: MesaDto? get() = mesasLibres.firstOrNull { it.id == mesaId }
    val totalPlatillos: Int get() = cantidades.values.sum()
    val total: BigDecimal
        get() = carta.fold(BigDecimal.ZERO) { suma, platillo ->
            suma + platillo.precio * BigDecimal(cantidadDe(platillo.id))
        }

    fun cantidadDe(platilloId: Long): Int = cantidades[platilloId] ?: 0
}

/** Formato del prototipo: soles con 2 decimales y coma. */
fun precioConFormato(valor: BigDecimal): String =
    "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')
