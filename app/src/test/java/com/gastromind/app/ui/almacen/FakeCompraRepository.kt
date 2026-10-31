package com.gastromind.app.ui.almacen

import com.gastromind.app.data.remote.dto.CompraOpcionesResponse
import com.gastromind.app.data.remote.dto.IngresoCompraRequest
import com.gastromind.app.data.remote.dto.IngresoCompraResponse
import com.gastromind.app.data.remote.dto.InsumoOpcion
import com.gastromind.app.data.remote.dto.ProveedorOpcion
import com.gastromind.app.data.repository.CompraRepository
import com.gastromind.app.data.repository.Resultado
import java.math.BigDecimal

class FakeCompraRepository : CompraRepository {
    var opciones: Resultado<CompraOpcionesResponse> = Resultado.Exito(
        CompraOpcionesResponse(
            listOf(ProveedorOpcion(1, "Mercado Mayorista")),
            listOf(InsumoOpcion(5, "Arroz", "kg", BigDecimal("25.500")), InsumoOpcion(6, "Sal", "kg", BigDecimal("2.000")))
        )
    )
    var respuesta: Resultado<IngresoCompraResponse> =
        Resultado.Exito(IngresoCompraResponse(9, 5, "Arroz", "kg", BigDecimal("36.000")))
    val enviados = mutableListOf<IngresoCompraRequest>()
    var cargas = 0

    override suspend fun opciones(): Resultado<CompraOpcionesResponse> {
        cargas++
        return opciones
    }

    override suspend fun registrar(request: IngresoCompraRequest): Resultado<IngresoCompraResponse> {
        enviados += request
        return respuesta
    }
}
