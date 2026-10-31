package com.gastromind.app.ui.almacen

import com.gastromind.app.data.remote.dto.InsumoOpcion
import com.gastromind.app.data.remote.dto.ProveedorOpcion
import java.math.BigDecimal
import java.time.LocalDate

data class IngresoCompraUiState(
    val cargando: Boolean = true,
    /** No se pudieron traer los proveedores y los insumos: se ofrece reintentar. */
    val errorCarga: String? = null,
    val proveedores: List<ProveedorOpcion> = emptyList(),
    val insumos: List<InsumoOpcion> = emptyList(),
    val proveedorId: Long? = null,
    val insumoId: Long? = null,
    val cantidad: String = "",
    val costoTotal: String = "",
    val lote: String = "",
    val vencimiento: LocalDate? = null,
    val guardando: Boolean = false,
    val error: String? = null,
    val exito: String? = null
) {
    val insumo: InsumoOpcion? get() = insumos.firstOrNull { it.id == insumoId }
    val proveedor: ProveedorOpcion? get() = proveedores.firstOrNull { it.id == proveedorId }

    /** Cómo queda el stock con lo que se está escribiendo; null si todavía no se puede calcular. */
    val stockDespues: BigDecimal?
        get() {
            val cantidadValida = leerDecimal(cantidad)?.takeIf { it > BigDecimal.ZERO } ?: return null
            return insumo?.let { it.stockActual + cantidadValida }
        }
}

/** Acepta coma o punto, como se escribe en el teclado del celular. */
fun leerDecimal(texto: String): BigDecimal? = texto.trim().replace(',', '.').toBigDecimalOrNull()

/** Formato del prototipo: 3 decimales con coma. */
fun cantidadConFormato(valor: BigDecimal): String = valor.setScale(3, java.math.RoundingMode.DOWN).toPlainString().replace('.', ',')
