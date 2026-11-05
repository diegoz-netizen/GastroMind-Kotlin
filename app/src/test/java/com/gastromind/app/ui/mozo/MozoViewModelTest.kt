package com.gastromind.app.ui.mozo

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.remote.dto.ItemPedido
import com.gastromind.app.data.remote.dto.MesaDto
import com.gastromind.app.data.remote.dto.PedidoRequest
import com.gastromind.app.data.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class MozoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakePedidoRepository()

    private fun vm() = MozoViewModel(repo)

    @Test
    fun alAbrir_TraeLasMesasYLaCarta() {
        val s = vm().uiState.value
        assertFalse(s.cargando)
        assertEquals(3, s.mesas.size)
        assertEquals(2, s.carta.size)
    }

    @Test
    fun mesasLibres_NoIncluyeLasOcupadas() {
        assertEquals(listOf(1L, 3L), vm().uiState.value.mesasLibres.map { it.id })
    }

    @Test
    fun siNoCarga_MuestraElErrorYReintentaBien() {
        val bien = repo.carta
        repo.carta = Resultado.Error("No se pudo conectar")
        val vm = vm()
        assertEquals("No se pudo conectar", vm.uiState.value.errorCarga)

        repo.carta = bien
        vm.cargar()

        assertNull(vm.uiState.value.errorCarga)
        assertEquals(2, vm.uiState.value.carta.size)
    }

    @Test
    fun masYMenos_LlevanLaCuentaYNoBajanDeCero() {
        val vm = vm()
        vm.onMas(10)
        vm.onMas(10)
        assertEquals(2, vm.uiState.value.cantidadDe(10))

        vm.onMenos(10)
        vm.onMenos(10)
        vm.onMenos(10)

        assertEquals(0, vm.uiState.value.cantidadDe(10))
        assertTrue(vm.uiState.value.cantidades.isEmpty())
    }

    @Test
    fun total_SumaPrecioPorCantidad() {
        val vm = vm()
        vm.onMas(10)
        vm.onMas(10)
        vm.onMas(11)

        assertEquals(3, vm.uiState.value.totalPlatillos)
        assertEquals(0, BigDecimal("72.50").compareTo(vm.uiState.value.total))
    }

    @Test
    fun enviar_SinMesa_PideElegirla() {
        val vm = vm()
        vm.onMas(10)
        vm.enviar()

        assertEquals("Elige la mesa.", vm.uiState.value.error)
        assertTrue(repo.enviados.isEmpty())
    }

    @Test
    fun enviar_SinPlatillos_PideAgregarUno() {
        val vm = vm()
        vm.onMesa(1)
        vm.enviar()

        assertEquals("Agrega al menos un platillo.", vm.uiState.value.error)
        assertTrue(repo.enviados.isEmpty())
    }

    @Test
    fun enviar_NoDejaElegirUnaMesaOcupada() {
        val vm = vm()
        vm.onMesa(2)
        vm.onMas(10)
        vm.enviar()

        assertEquals("Elige la mesa.", vm.uiState.value.error)
        assertTrue(repo.enviados.isEmpty())
    }

    @Test
    fun enviar_MandaLaMesaYLosPlatillosEnElOrdenDeLaCarta() {
        val vm = vm()
        vm.onMesa(1)
        vm.onMas(11)
        vm.onMas(10)
        vm.onMas(10)
        vm.enviar()

        val esperado = PedidoRequest(1, listOf(ItemPedido(10, 2), ItemPedido(11, 1)))
        assertEquals(esperado, repo.enviados.single())
    }

    @Test
    fun enviar_Exitoso_LimpiaLaCuentaYRefrescaLasMesas() {
        val vm = vm()
        vm.onMesa(1)
        vm.onMas(10)
        vm.enviar()

        val s = vm.uiState.value
        assertFalse(s.enviando)
        assertTrue(s.exito!!.contains("mesa 1"))
        assertNull(s.mesaId)
        assertTrue(s.cantidades.isEmpty())
        // Una al abrir y otra al terminar de enviar
        assertEquals(2, repo.cargasDeMesas)
    }

    @Test
    fun enviar_ConError_MuestraElMensajeYConservaLaCuenta() {
        repo.respuesta = Resultado.Error("El platillo no está disponible")
        val vm = vm()
        vm.onMesa(1)
        vm.onMas(10)
        vm.enviar()

        val s = vm.uiState.value
        assertEquals("El platillo no está disponible", s.error)
        assertFalse(s.enviando)
        assertEquals(1, s.cantidadDe(10))
        assertEquals(1L, s.mesaId)
    }

    @Test
    fun enviar_SiLaMesaYaNoEstaLibre_LaQuitaDeLaSeleccionYGuardaLaCuenta() {
        repo.respuesta = Resultado.Error("La mesa no está libre")
        val vm = vm()
        vm.onMesa(1)
        vm.onMas(10)
        // Mientras tanto, otro mozo tomó la mesa 1
        repo.mesas = Resultado.Exito(listOf(MesaDto(1, 1, "OCUPADA"), MesaDto(3, 3, "LIBRE")))
        vm.enviar()

        val s = vm.uiState.value
        assertEquals("La mesa no está libre", s.error)
        assertNull(s.mesaId)
        assertEquals(listOf(3L), s.mesasLibres.map { it.id })
        assertEquals(1, s.cantidadDe(10))
    }
}
