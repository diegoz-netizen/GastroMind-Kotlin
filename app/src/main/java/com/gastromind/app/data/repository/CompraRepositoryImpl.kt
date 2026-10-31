package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.GastroMindApi
import com.gastromind.app.data.remote.dto.CompraOpcionesResponse
import com.gastromind.app.data.remote.dto.IngresoCompraRequest
import com.gastromind.app.data.remote.dto.IngresoCompraResponse
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException

class CompraRepositoryImpl(private val api: GastroMindApi) : CompraRepository {

    override suspend fun opciones(): Resultado<CompraOpcionesResponse> = llamar { api.opcionesDeCompra() }

    override suspend fun registrar(request: IngresoCompraRequest): Resultado<IngresoCompraResponse> =
        llamar { api.registrarIngreso(request) }

    private suspend fun <T> llamar(llamada: suspend () -> T): Resultado<T> = try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }
}
