package com.gastromind.app.ui.almacen

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class IngresoCompraViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val hoy = LocalDate.of(2026, 10, 31)
    private val repo = FakeCompraRepository()

    private fun vm() = IngresoCompraViewModel(repo) { hoy }

    private fun formularioValido(vm: IngresoCompraViewModel) = vm.apply {
        onProveedor(1)
        onInsumo(5)
        onCantidad("10,5")
        onCostoTotal("42.50")
    }

    @Test
    fun alAbrir_TraeProveedoresEInsumos() {
        val s = vm().uiState.value
        assertFalse(s.cargando)
        assertEquals(1, s.proveedores.size)
        assertEquals(2, s.insumos.size)
    }

    @Test
    fun siNoCarga_MuestraElErrorYReintentaBien() {
        val bien = repo.opciones
        repo.opciones = Resultado.Error("No se pudo conectar")
        val vm = vm()
        assertEquals("No se pudo conectar", vm.uiState.value.errorCarga)

        repo.opciones = bien
        vm.cargarOpciones()

        assertNull(vm.uiState.value.errorCarga)
        assertEquals(2, vm.uiState.value.insumos.size)
        assertEquals(2, repo.cargas)
    }

    @Test
    fun stockDespues_SumaLaCantidadAlInsumoElegido() {
        val vm = vm()
        assertNull(vm.uiState.value.stockDespues)
        vm.onInsumo(5)
        vm.onCantidad("10,5")
        assertEquals(BigDecimal("36.000"), vm.uiState.value.stockDespues)
        vm.onCantidad("abc")
        assertNull(vm.uiState.value.stockDespues)
    }

    @Test
    fun registrar_EnviaLoEscritoAceptandoComaOPunto() {
        val vm = formularioValido(vm())
        vm.onLote("  L-7  ")
        vm.onVencimiento(hoy)

        vm.registrar()

        val enviado = repo.enviados.single()
        assertEquals(1L, enviado.proveedorId)
        assertEquals(5L, enviado.ingredienteId)
        assertEquals(BigDecimal("10.5"), enviado.cantidad)
        assertEquals(BigDecimal("42.50"), enviado.costoTotal)
        assertEquals("L-7", enviado.lote)
        assertEquals("2026-10-31", enviado.fechaVencimiento)
    }

    @Test
    fun loteYVencimientoOpcionales_VanEnNull() {
        val vm = formularioValido(vm())
        vm.onLote("   ")

        vm.registrar()

        assertNull(repo.enviados.single().lote)
        assertNull(repo.enviados.single().fechaVencimiento)
    }

    @Test
    fun alRegistrar_LimpiaElFormularioYActualizaElStock() {
        val vm = formularioValido(vm())

        vm.registrar()

        val s = vm.uiState.value
        assertEquals("Ingreso registrado. Arroz quedó en 36,000 kg.", s.exito)
        assertEquals(BigDecimal("36.000"), s.insumos.first { it.id == 5L }.stockActual)
        assertEquals(1L, s.proveedorId)
        assertNull(s.insumoId)
        assertEquals("", s.cantidad)
        assertEquals("", s.costoTotal)
        assertFalse(s.guardando)
    }

    @Test
    fun siLaApiRechaza_MuestraElMensajeYConservaLoEscrito() {
        repo.respuesta = Resultado.Error("Proveedor no encontrado")
        val vm = formularioValido(vm())

        vm.registrar()

        val s = vm.uiState.value
        assertEquals("Proveedor no encontrado", s.error)
        assertEquals("10,5", s.cantidad)
        assertFalse(s.guardando)
    }

    @Test
    fun datosInvalidos_NoLlamanALaApi() {
        fun rechaza(mensaje: String, preparar: IngresoCompraViewModel.() -> Unit) {
            val vm = vm()
            vm.preparar()
            vm.registrar()
            assertEquals(mensaje, vm.uiState.value.error)
        }

        rechaza("Elige el insumo.") { onProveedor(1); onCantidad("1"); onCostoTotal("1") }
        rechaza("Elige el proveedor.") { onInsumo(5); onCantidad("1"); onCostoTotal("1") }
        rechaza("La cantidad debe ser mayor que 0.") { formularioValido(this); onCantidad("0") }
        rechaza("La cantidad admite hasta 3 decimales.") { formularioValido(this); onCantidad("1,2345") }
        rechaza("Escribe el costo total.") { formularioValido(this); onCostoTotal("") }
        rechaza("Escribe el costo total.") { formularioValido(this); onCostoTotal("-1") }
        rechaza("El costo total admite hasta 2 decimales.") { formularioValido(this); onCostoTotal("1,234") }
        rechaza("El lote admite hasta 50 caracteres.") { formularioValido(this); onLote("x".repeat(51)) }
        rechaza("La fecha de vencimiento ya pasó.") { formularioValido(this); onVencimiento(hoy.minusDays(1)) }

        assertTrue(repo.enviados.isEmpty())
    }

    @Test
    fun costoCeroYVenceHoy_SonValidos() {
        val vm = formularioValido(vm())
        vm.onCostoTotal("0")
        vm.onVencimiento(hoy)

        vm.registrar()

        assertEquals(1, repo.enviados.size)
    }
}
