package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.AnularPlatilloRequest
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException

class AnularRepositoryImpl(private val api: GastroMindApi) : AnularRepository {

    override suspend fun motivos() = llamar { api.motivosDeMerma() }

    override suspend fun anular(pedidoId: Long, platilloId: Long, motivoId: Long?) =
        llamar { api.anularPlatillo(pedidoId, platilloId, AnularPlatilloRequest(motivoId)).aPedido() }

    private suspend fun <T> llamar(llamada: suspend () -> T): Resultado<T> = try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }
}
