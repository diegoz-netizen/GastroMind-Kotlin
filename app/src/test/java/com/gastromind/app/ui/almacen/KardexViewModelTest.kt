package com.gastromind.app.ui.almacen

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.remote.dto.MovimientoKardex
import com.gastromind.app.data.remote.dto.OrigenKardex
import com.gastromind.app.data.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class KardexViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakeKardexRepository()
    private val dia1 = LocalDate.of(2026, 10, 20)
    private val dia2 = LocalDate.of(2026, 10, 23)

    private fun movimiento(tipo: String, empleado: String? = "Ana", origen: OrigenKardex? = null) = MovimientoKardex(
        1, "2026-10-26T16:13:35.629197-05:00", tipo, BigDecimal("1.500"), null, BigDecimal("2.000"), empleado, origen
    )

    private fun vm() = KardexViewModel(repo).apply { actualizar() }

    @Test
    fun alAbrir_TraeLosInsumosYNoConsultaHastaElegirUno() {
        val vm = vm()
        assertFalse(vm.uiState.value.cargandoInsumos)
        assertEquals(listOf(repo.arroz), vm.uiState.value.insumos)
        assertTrue(repo.consultas.isEmpty())
    }

    @Test
    fun siNoCarganLosInsumos_MuestraElErrorYReintentaBien() {
        val bien = repo.insumos
        repo.insumos = Resultado.Error("No se pudo conectar")
        val vm = vm()
        assertEquals("No se pudo conectar", vm.uiState.value.errorInsumos)

        repo.insumos = bien
        vm.actualizar()

        assertNull(vm.uiState.value.errorInsumos)
        assertEquals(1, vm.uiState.value.insumos.size)
    }

    @Test
    fun alElegirInsumo_TraeSusMovimientos() {
        repo.movimientos = listOf(movimiento("INGRESO"))
        val vm = vm()

        vm.onInsumo(5)

        assertEquals(1, vm.uiState.value.movimientos.size)
        assertFalse(vm.uiState.value.cargandoMovimientos)
        assertEquals(Triple(5L, null, null), repo.consultas.single())
    }

    @Test
    fun lasFechas_FiltranYSePuedenQuitar() {
        val vm = vm()
        vm.onInsumo(5)
        vm.onDesde(dia1)
        vm.onHasta(dia2)
        assertEquals(Triple<Long, LocalDate?, LocalDate?>(5L, dia1, dia2), repo.consultas.last())

        vm.onDesde(null)
        assertEquals(Triple<Long, LocalDate?, LocalDate?>(5L, null, dia2), repo.consultas.last())
    }

    @Test
    fun rangoInvertido_AvisaYNoConsulta() {
        val vm = vm()
        vm.onInsumo(5)
        vm.onDesde(dia2)
        val antes = repo.consultas.size

        vm.onHasta(dia1)

        assertEquals("La fecha inicial no puede ser mayor que la final.", vm.uiState.value.error)
        assertEquals(antes, repo.consultas.size)
    }

    @Test
    fun siLaConsultaFalla_MuestraElMensajeSinMovimientos() {
        repo.movimientos = listOf(movimiento("INGRESO"))
        val vm = vm()
        vm.onInsumo(5)

        repo.error = "No se pudo conectar"
        vm.onDesde(dia1)

        assertEquals("No se pudo conectar", vm.uiState.value.error)
        assertTrue(vm.uiState.value.movimientos.isEmpty())
    }

    @Test
    fun alVolverALaPestana_ActualizaInsumosYMovimientos() {
        val vm = vm()
        vm.onInsumo(5)
        val consultas = repo.consultas.size

        vm.actualizar()

        assertEquals(2, repo.cargasDeInsumos)
        assertEquals(consultas + 1, repo.consultas.size)
    }

    @Test
    fun tituloYDetalle_ComoEnElPrototipo() {
        assertEquals("Saldo inicial", tituloDe(movimiento("AJUSTE", empleado = null)))
        assertEquals("Ajuste", tituloDe(movimiento("AJUSTE")))
        assertEquals("Ingreso de compra", tituloDe(movimiento("INGRESO")))
        assertEquals("Venta", tituloDe(movimiento("CONSUMO")))
        assertEquals("Merma", tituloDe(movimiento("MERMA")))

        assertEquals(
            "Carnes del Sur · lote L-7 · S/ 21,50",
            detalleDe(movimiento("INGRESO", origen = OrigenKardex(proveedor = "Carnes del Sur", lote = "L-7", costoTotal = BigDecimal("21.50"))))
        )
        assertEquals(
            "Lomo saltado · pedido 4",
            detalleDe(movimiento("CONSUMO", origen = OrigenKardex(platillo = "Lomo saltado", pedidoId = 4)))
        )
        assertEquals("Mal manejo · Ana", detalleDe(movimiento("MERMA", origen = OrigenKardex(motivo = "MAL_MANEJO"))))
        assertNull(detalleDe(movimiento("AJUSTE", empleado = null)))
    }

    @Test
    fun cifras_ConSignoYTresDecimales() {
        assertEquals("26/10/2026", diaDe(movimiento("INGRESO")))
        assertEquals("+1,500", entradaConFormato(BigDecimal("1.5")))
        assertEquals("−0,300", salidaConFormato(BigDecimal("0.300")))
        assertEquals("", entradaConFormato(null))
    }
}
