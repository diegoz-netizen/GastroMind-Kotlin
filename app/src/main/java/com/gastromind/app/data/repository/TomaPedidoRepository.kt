package com.gastromind.app.data.repository

import java.math.BigDecimal

data class Mesa(val id: Long, val numero: Int, val libre: Boolean)

data class LineaCuenta(val producto: String, val cantidad: Int, val precioUnitario: BigDecimal, val subtotal: BigDecimal)

/** HU-12: la cuenta de la mesa al cerrarla, con lo servido. */
data class Cuenta(val mesaNumero: Int, val lineas: List<LineaCuenta>, val total: BigDecimal)

data class PlatilloCarta(val id: Long, val nombre: String, val precio: BigDecimal, val categoria: String)

/**
 * HU-09: lo que necesita el mozo para tomar un pedido. Las mesas y los pedidos vienen de Spring y la carta
 * de Django, con el mismo JWT.
 */
interface TomaPedidoRepository {
    suspend fun mesas(): Resultado<List<Mesa>>
    suspend fun carta(): Resultado<List<PlatilloCarta>>

    /** El pedido sin cerrar de la mesa, o null si no tiene. */
    suspend fun pedidoDeMesa(mesaId: Long): Resultado<Pedido?>

    /** Sin pedidoId crea el pedido (mesa libre); con pedidoId suma una segunda ronda. items: productoId → cantidad. */
    suspend fun enviar(mesaId: Long, pedidoId: Long?, items: Map<Long, Int>): Resultado<Pedido>

    /** HU-12 (CA-12): cierra la mesa y devuelve la cuenta. El backend no deja cerrar con platillos sin servir. */
    suspend fun cerrar(pedidoId: Long): Resultado<Cuenta>
}
