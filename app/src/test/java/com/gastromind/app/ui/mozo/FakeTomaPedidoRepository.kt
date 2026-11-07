package com.gastromind.app.ui.mozo

import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.repository.Mesa
import com.gastromind.app.data.repository.Pedido
import com.gastromind.app.data.repository.Platillo
import com.gastromind.app.data.repository.PlatilloCarta
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.data.repository.TomaPedidoRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class FakeTomaPedidoRepository : TomaPedidoRepository {
    var mesas: Resultado<List<Mesa>> = Resultado.Exito(listOf(Mesa(1, 1, true), Mesa(2, 2, false)))
    var carta: Resultado<List<PlatilloCarta>> = Resultado.Exito(
        listOf(
            PlatilloCarta(10, "Lomo saltado", BigDecimal("32.00"), "Fondos"),
            PlatilloCarta(11, "Chicha morada", BigDecimal("8.50"), "Bebidas")
        )
    )
    var pedidoDeMesa: Resultado<Pedido?> = Resultado.Exito(null)
    var respuesta: Resultado<Pedido> = Resultado.Exito(pedido(7))

    data class Envio(val mesaId: Long, val pedidoId: Long?, val items: Map<Long, Int>)
    val enviados = mutableListOf<Envio>()

    override suspend fun mesas() = mesas
    override suspend fun carta() = carta
    override suspend fun pedidoDeMesa(mesaId: Long) = pedidoDeMesa

    override suspend fun enviar(mesaId: Long, pedidoId: Long?, items: Map<Long, Int>): Resultado<Pedido> {
        enviados += Envio(mesaId, pedidoId, items)
        return respuesta
    }

    companion object {
        fun pedido(id: Long) = Pedido(id, 2, LocalDateTime.of(2026, 11, 3, 13, 0),
            listOf(Platillo(100, "Lomo saltado", 1, EstadoPlatillo.SERVIDO)))
    }
}
