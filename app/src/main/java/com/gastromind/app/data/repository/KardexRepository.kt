package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.dto.InsumoDto
import com.gastromind.app.data.remote.dto.KardexResponse
import java.time.LocalDate

/** HU-15: consulta del kardex de un insumo. */
interface KardexRepository {
    suspend fun insumos(): Resultado<List<InsumoDto>>
    suspend fun kardex(insumoId: Long, desde: LocalDate?, hasta: LocalDate?): Resultado<KardexResponse>
}
