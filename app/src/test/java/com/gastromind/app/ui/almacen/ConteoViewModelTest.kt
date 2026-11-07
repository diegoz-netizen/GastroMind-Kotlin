package com.gastromind.app.ui.almacen

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.remote.dto.ConteoRequest
import com.gastromind.app.data.remote.dto.ConteoResponse
import com.gastromind.app.data.repository.ConteoRepository
import com.gastromind.app.data.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

private class FakeConteoRepository : ConteoRepository {
    var respuesta: Resultado<ConteoResponse> = Resultado.Exito(
        ConteoResponse(1, 5, "Arroz", "kg", BigDecimal("24.800"), BigDecimal("25.500"), BigDecimal("-0.700"))
    )
    val enviados = mutableListOf<ConteoRequest>()

    override suspend fun registrar(request: ConteoRequest): Resultado<ConteoResponse> {
        enviados += request
        return respuesta
    }
}

class ConteoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val compras = FakeCompraRepository()
    private val repo = FakeConteoRepository()

    private fun vm() = ConteoViewModel(compras, repo)

    @Test
    fun alAbrir_TraeLosInsumosConSuStock() {
        val s = vm().uiState.value
        assertFalse(s.cargando)
        assertEquals(listOf("Arroz", "Sal"), s.insumos.map { it.nombre })
    }

    @Test
    fun registrar_EnviaLoContadoConComaYMuestraLaDiferencia() {
        val vm = vm()
        vm.onInsumo(5)
        vm.onCantidad("24,8")
        vm.registrar()
        assertEquals(listOf(ConteoRequest(5, BigDecimal("24.8"))), repo.enviados)
        val s = vm.uiState.value
        assertEquals(BigDecimal("-0.700"), s.ultimo?.diferencia)
        assertEquals("", s.cantidad)
        assertFalse(s.guardando)
    }

    @Test
    fun contarCero_EsValido() {
        val vm = vm()
        vm.onInsumo(6)
        vm.onCantidad("0")
        vm.registrar()
        assertEquals(BigDecimal("0"), repo.enviados.single().cantidad)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun sinInsumoSinCantidadNegativaOConCuatroDecimales_NoEnvia() {
        val vm = vm()
        vm.registrar()
        assertEquals("Elige el insumo.", vm.uiState.value.error)
        vm.onInsumo(5)
        vm.registrar()
        assertEquals("Escribe la cantidad contada.", vm.uiState.value.error)
        vm.onCantidad("-1")
        vm.registrar()
        assertEquals("La cantidad contada no puede ser negativa.", vm.uiState.value.error)
        vm.onCantidad("1,0005")
        vm.registrar()
        assertEquals("La cantidad admite hasta 3 decimales.", vm.uiState.value.error)
        assertTrue(repo.enviados.isEmpty())
    }

    @Test
    fun siElBackendRechaza_MuestraElMensajeYConservaLoEscrito() {
        repo.respuesta = Resultado.Error("Insumo no encontrado")
        val vm = vm()
        vm.onInsumo(5)
        vm.onCantidad("3")
        vm.registrar()
        val s = vm.uiState.value
        assertEquals("Insumo no encontrado", s.error)
        assertEquals("3", s.cantidad)
        assertNull(s.ultimo)
    }

    @Test
    fun alVolverALaPestana_ReleeElStock() {
        val vm = vm()
        val antes = compras.cargas
        vm.actualizar()
        assertEquals(antes + 1, compras.cargas)
    }
}
