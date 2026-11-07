package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.dto.ConteoRequest
import com.gastromind.app.data.remote.dto.ConteoResponse

/** HU-30: conteo físico de un insumo. */
interface ConteoRepository {
    suspend fun registrar(request: ConteoRequest): Resultado<ConteoResponse>
}
