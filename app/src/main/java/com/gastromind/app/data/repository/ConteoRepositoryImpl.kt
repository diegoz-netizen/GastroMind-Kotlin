package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.ConteoRequest
import com.gastromind.app.data.remote.dto.ConteoResponse
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException

class ConteoRepositoryImpl(private val api: GastroMindApi) : ConteoRepository {

    override suspend fun registrar(request: ConteoRequest): Resultado<ConteoResponse> = try {
        Resultado.Exito(api.registrarConteo(request))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }
}
