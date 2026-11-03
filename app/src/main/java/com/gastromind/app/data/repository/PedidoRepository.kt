package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.dto.PedidoResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Estados de un platillo en el backend (EstadoPlatillo). */
enum class EstadoPlatillo { PENDIENTE, EN_PREPARACION, LISTO, SERVIDO, ANULADO, DESCONOCIDO;

    companion object {
        fun de(nombre: String?): EstadoPlatillo = entries.firstOrNull { it.name == nombre } ?: DESCONOCIDO
    }
}

data class Platillo(val id: Long, val nombre: String, val cantidad: Int, val estado: EstadoPlatillo)

/** fechaRegistro ya en la hora del dispositivo, la que se muestra y con la que se cuentan los minutos. */
data class Pedido(val id: Long, val mesaNumero: Int, val fechaRegistro: LocalDateTime, val platillos: List<Platillo>)

fun PedidoResponse.aPedido(zona: ZoneId = ZoneId.systemDefault()) = Pedido(
    id = id,
    mesaNumero = mesaNumero,
    fechaRegistro = LocalDateTime.parse(fechaRegistro).atOffset(ZoneOffset.UTC).atZoneSameInstant(zona).toLocalDateTime(),
    platillos = detalles.map { Platillo(it.id, it.producto, it.cantidad, EstadoPlatillo.de(it.estado)) }
)

/** Pedidos en curso del restaurante: los lee cocina (HU-10) y el mozo (HU-11); cada uno avanza lo que le toca. */
interface PedidoRepository {
    suspend fun vigentes(): Resultado<List<Pedido>>
    suspend fun cambiarEstado(pedidoId: Long, platilloId: Long, nuevo: EstadoPlatillo): Resultado<Pedido>
}
