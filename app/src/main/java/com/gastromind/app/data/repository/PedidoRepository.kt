package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.dto.MesaDto
import com.gastromind.app.data.remote.dto.PedidoCreado
import com.gastromind.app.data.remote.dto.PedidoRequest
import com.gastromind.app.data.remote.dto.PlatilloCarta

/** HU-09: el mozo elige la mesa, arma el pedido con la carta y lo envía a cocina. */
interface PedidoRepository {
    suspend fun mesas(): Resultado<List<MesaDto>>

    /** Solo los platillos activos: lo desactivado no se puede pedir. */
    suspend fun carta(): Resultado<List<PlatilloCarta>>

    suspend fun crear(request: PedidoRequest): Resultado<PedidoCreado>
}
