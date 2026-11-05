package com.gastromind.app.ui.mozo

import com.gastromind.app.data.remote.dto.MesaDto
import com.gastromind.app.data.remote.dto.PedidoCreado
import com.gastromind.app.data.remote.dto.PedidoRequest
import com.gastromind.app.data.remote.dto.PlatilloCarta
import com.gastromind.app.data.repository.PedidoRepository
import com.gastromind.app.data.repository.Resultado
import java.math.BigDecimal

class FakePedidoRepository : PedidoRepository {
    var mesas: Resultado<List<MesaDto>> = Resultado.Exito(
        listOf(MesaDto(1, 1, "LIBRE"), MesaDto(2, 2, "OCUPADA"), MesaDto(3, 3, "LIBRE"))
    )
    var carta: Resultado<List<PlatilloCarta>> = Resultado.Exito(
        listOf(
            PlatilloCarta(10, "Lomo saltado", BigDecimal("32.00"), "Platos"),
            PlatilloCarta(11, "Chicha morada", BigDecimal("8.50"), "Bebidas")
        )
    )
    var respuesta: Resultado<PedidoCreado> = Resultado.Exito(PedidoCreado(7, 1, 1, "ABIERTO"))
    val enviados = mutableListOf<PedidoRequest>()
    var cargasDeMesas = 0

    override suspend fun mesas(): Resultado<List<MesaDto>> {
        cargasDeMesas++
        return mesas
    }

    override suspend fun carta(): Resultado<List<PlatilloCarta>> = carta

    override suspend fun crear(request: PedidoRequest): Resultado<PedidoCreado> {
        enviados += request
        return respuesta
    }
}
