package com.gastromind.app.ui.almacen

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.remote.dto.MermaRequest
import com.gastromind.app.data.remote.dto.MermaResponse
import com.gastromind.app.data.remote.dto.MotivoMermaDto
import com.gastromind.app.data.repository.MermaRepository
import com.gastromind.app.data.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

private class FakeMermaRepository : MermaRepository {
    var motivos: Resultado<List<MotivoMermaDto>> = Resultado.Exito(
        listOf(MotivoMermaDto(1, "VENCIDO"), MotivoMermaDto(2, "MAL_MANEJO"), MotivoMermaDto(8, "PLATO_ANULADO"))
    )
    var respuesta: Resultado<MermaResponse> = Resultado.Exito(
        MermaResponse(10, "Arroz", BigDecimal("0.500"), "kg", "VENCIDO", BigDecimal("25.000"), BigDecimal("2.25"), "Almacenero")
    )
    val enviados = mutableListOf<MermaRequest>()

    override suspend fun motivos() = motivos

    override suspend fun registrar(request: MermaRequest): Resultado<MermaResponse> {
        enviados += request
        return respuesta
    }
}

class MermaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val compras = FakeCompraRepository()
    private val repo = FakeMermaRepository()

    private fun vm() = MermaViewModel(compras, repo)

    @Test
    fun alAbrir_TraeInsumosYMotivosSinElDeCocina() {
        val s = vm().uiState.value
        assertFalse(s.cargando)
        assertNull(s.errorCarga)
        assertEquals(listOf("Arroz", "Sal"), s.insumos.map { it.nombre })
        // PLATO_ANULADO lo registra la cocina al anular (HU-18), no el almacén
        assertEquals(listOf("VENCIDO", "MAL_MANEJO"), s.motivos.map { it.nombre })
    }

    @Test
    fun registrar_EnviaInsumoCantidadConComaYMotivo_YMuestraElResultado() {
        val vm = vm()
        vm.onInsumo(5)
        vm.onCantidad("0,5")
        vm.onMotivo(1)
        vm.registrar()
        assertEquals(listOf(MermaRequest(5, BigDecimal("0.5"), 1)), repo.enviados)
        val s = vm.uiState.value
        assertEquals(BigDecimal("25.000"), s.ultima?.stockDespues)
        assertEquals("", s.cantidad)
        assertNull(s.motivoId)
        assertFalse(s.guardando)
    }

    @Test
    fun sinInsumoCantidadOMotivo_NoEnviaYDiceQueFalta() {
        val vm = vm()
        vm.registrar()
        assertEquals("Elige el insumo.", vm.uiState.value.error)
        vm.onInsumo(5)
        vm.registrar()
        assertEquals("Escribe la cantidad perdida.", vm.uiState.value.error)
        vm.onCantidad("0")
        vm.registrar()
        assertEquals("La cantidad debe ser mayor que 0.", vm.uiState.value.error)
        vm.onCantidad("1,0005")
        vm.registrar()
        assertEquals("La cantidad admite hasta 3 decimales.", vm.uiState.value.error)
        vm.onCantidad("1")
        vm.registrar()
        assertEquals("Elige el motivo.", vm.uiState.value.error)
        assertTrue(repo.enviados.isEmpty())
    }

    @Test
    fun errorDelBackend_SeMuestraYSeConservaLoEscrito() {
        repo.respuesta = Resultado.Error("Insumo no encontrado")
        val vm = vm()
        vm.onInsumo(5)
        vm.onCantidad("2")
        vm.onMotivo(2)
        vm.registrar()
        val s = vm.uiState.value
        assertEquals("Insumo no encontrado", s.error)
        assertEquals("2", s.cantidad)
        assertEquals(2L, s.motivoId)
        assertFalse(s.guardando)
    }

    @Test
    fun sinMotivosAlAbrir_MuestraErrorDeCarga() {
        repo.motivos = Resultado.Error("No se pudo conectar")
        val s = vm().uiState.value
        assertEquals("No se pudo conectar", s.errorCarga)
    }
}
