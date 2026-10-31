package com.gastromind.app.ui.almacen

import com.gastromind.app.data.remote.dto.InsumoDto
import com.gastromind.app.data.remote.dto.KardexResponse
import com.gastromind.app.data.remote.dto.MovimientoKardex
import com.gastromind.app.data.repository.KardexRepository
import com.gastromind.app.data.repository.Resultado
import java.math.BigDecimal
import java.time.LocalDate

class FakeKardexRepository : KardexRepository {
    val arroz = InsumoDto(5, "Arroz", "kg", BigDecimal("25.000"))
    var insumos: Resultado<List<InsumoDto>> = Resultado.Exito(listOf(arroz))
    var movimientos: List<MovimientoKardex> = emptyList()
    var error: String? = null
    val consultas = mutableListOf<Triple<Long, LocalDate?, LocalDate?>>()
    var cargasDeInsumos = 0

    override suspend fun insumos(): Resultado<List<InsumoDto>> {
        cargasDeInsumos++
        return insumos
    }

    override suspend fun kardex(insumoId: Long, desde: LocalDate?, hasta: LocalDate?): Resultado<KardexResponse> {
        consultas += Triple(insumoId, desde, hasta)
        return error?.let { Resultado.Error(it) } ?: Resultado.Exito(KardexResponse(arroz, movimientos))
    }
}
