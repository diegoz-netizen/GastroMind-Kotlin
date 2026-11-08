package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.MermaRequest
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException

class MermaRepositoryImpl(private val api: GastroMindApi) : MermaRepository {

    override suspend fun motivos() = llamar { api.motivosDeMerma() }

    override suspend fun registrar(request: MermaRequest) = llamar { api.registrarMerma(request) }

    private suspend fun <T> llamar(llamada: suspend () -> T): Resultado<T> = try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }
}
