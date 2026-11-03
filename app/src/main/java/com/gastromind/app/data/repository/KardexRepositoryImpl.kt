package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.DjangoApi
import com.gastromind.app.data.remote.dto.InsumoDto
import com.gastromind.app.data.remote.dto.KardexResponse
import com.gastromind.app.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

class KardexRepositoryImpl(private val api: DjangoApi) : KardexRepository {

    override suspend fun insumos(): Resultado<List<InsumoDto>> = llamar { api.insumos() }

    override suspend fun kardex(insumoId: Long, desde: LocalDate?, hasta: LocalDate?): Resultado<KardexResponse> =
        llamar { api.kardex(insumoId, desde?.toString(), hasta?.toString()) }

    private suspend fun <T> llamar(llamada: suspend () -> T): Resultado<T> = try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resultado.Error(mensajeDeError(e))
    }
}
