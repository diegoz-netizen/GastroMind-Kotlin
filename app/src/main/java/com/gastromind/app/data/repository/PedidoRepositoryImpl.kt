package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.DjangoApi
import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.MesaDto
import com.gastromind.app.data.remote.dto.PedidoCreado
import com.gastromind.app.data.remote.dto.PedidoRequest
import com.gastromind.app.data.remote.dto.PlatilloCarta
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException

/** Las mesas y el pedido van a Spring; la carta vive en Django (con el mismo JWT). */
class PedidoRepositoryImpl(
    private val api: GastroMindApi,
    private val django: DjangoApi
) : PedidoRepository {

    override suspend fun mesas(): Resultado<List<MesaDto>> = llamar { api.mesas() }

    override suspend fun carta(): Resultado<List<PlatilloCarta>> = llamar { django.productos(ESTADO_ACTIVO) }

    override suspend fun crear(request: PedidoRequest): Resultado<PedidoCreado> = llamar { api.crearPedido(request) }

    private suspend fun <T> llamar(llamada: suspend () -> T): Resultado<T> = try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }

    private companion object {
        const val ESTADO_ACTIVO = "ACTIVO"
    }
}
