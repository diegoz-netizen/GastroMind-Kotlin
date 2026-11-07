package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.dto.MotivoMermaDto

/** HU-18: anular desde cocina un platillo que ya se empezó a preparar; sus insumos se registran como merma. */
interface AnularRepository {
    suspend fun motivos(): Resultado<List<MotivoMermaDto>>
    suspend fun anular(pedidoId: Long, platilloId: Long, motivoId: Long?): Resultado<Pedido>
}
