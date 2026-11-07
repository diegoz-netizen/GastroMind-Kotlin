package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.DjangoApi
import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.AgregarPlatillosRequest
import com.gastromind.app.data.remote.dto.CrearPedidoRequest
import com.gastromind.app.data.remote.dto.ItemPedidoRequest
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException

class TomaPedidoRepositoryImpl(
    private val api: GastroMindApi,
    private val djangoApi: DjangoApi
) : TomaPedidoRepository {

    override suspend fun mesas(): Resultado<List<Mesa>> = llamar {
        api.mesas().map { Mesa(it.id, it.numero, it.estado == LIBRE) }.sortedBy { it.numero }
    }

    override suspend fun carta(): Resultado<List<PlatilloCarta>> = llamar {
        djangoApi.productos(ACTIVO).map { PlatilloCarta(it.id, it.nombre, it.precio, it.categoriaNombre ?: SIN_CATEGORIA) }
    }

    override suspend fun pedidoDeMesa(mesaId: Long): Resultado<Pedido?> = llamar {
        api.pedidosVigentes().firstOrNull { it.mesaId == mesaId }?.aPedido()
    }

    override suspend fun enviar(mesaId: Long, pedidoId: Long?, items: Map<Long, Int>): Resultado<Pedido> = llamar {
        val lineas = items.map { (productoId, cantidad) -> ItemPedidoRequest(productoId, cantidad) }
        val respuesta = if (pedidoId == null) api.crearPedido(CrearPedidoRequest(mesaId, lineas))
        else api.agregarPlatillos(pedidoId, AgregarPlatillosRequest(lineas))
        respuesta.aPedido()
    }

    private suspend fun <T> llamar(llamada: suspend () -> T): Resultado<T> = try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }

    private companion object {
        const val LIBRE = "LIBRE"
        const val ACTIVO = "ACTIVO"
        const val SIN_CATEGORIA = "Otros"
    }
}
