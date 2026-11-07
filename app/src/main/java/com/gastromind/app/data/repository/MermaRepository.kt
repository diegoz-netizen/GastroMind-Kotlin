package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.dto.MermaRequest
import com.gastromind.app.data.remote.dto.MermaResponse
import com.gastromind.app.data.remote.dto.MotivoMermaDto

/** HU-17: registrar una merma desde el almacén, con su motivo. */
interface MermaRepository {
    suspend fun motivos(): Resultado<List<MotivoMermaDto>>
    suspend fun registrar(request: MermaRequest): Resultado<MermaResponse>
}
