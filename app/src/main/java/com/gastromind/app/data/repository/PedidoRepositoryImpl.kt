package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.CambioEstadoRequest
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException

class PedidoRepositoryImpl(private val api: GastroMindApi) : PedidoRepository {

    override suspend fun vigentes(): Resultado<List<Pedido>> =
        llamar { api.pedidosVigentes().map { it.aPedido() } }

    override suspend fun cambiarEstado(pedidoId: Long, platilloId: Long, nuevo: EstadoPlatillo): Resultado<Pedido> =
        llamar { api.cambiarEstadoPlatillo(pedidoId, platilloId, CambioEstadoRequest(nuevo.name)).aPedido() }

    private suspend fun <T> llamar(llamada: suspend () -> T): Resultado<T> = try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }
}
