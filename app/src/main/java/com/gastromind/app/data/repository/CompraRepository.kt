package com.gastromind.app.data.repository

import com.gastromind.app.data.remote.dto.CompraOpcionesResponse
import com.gastromind.app.data.remote.dto.IngresoCompraRequest
import com.gastromind.app.data.remote.dto.IngresoCompraResponse

/** HU-14: ingreso de compras al almacén. */
interface CompraRepository {
    suspend fun opciones(): Resultado<CompraOpcionesResponse>
    suspend fun registrar(request: IngresoCompraRequest): Resultado<IngresoCompraResponse>
}
