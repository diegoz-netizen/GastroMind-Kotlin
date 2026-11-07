package com.gastromind.app.ui.mozo

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class PedidoMesaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakeTomaPedidoRepository()

    private fun vm(mesaId: Long = 1) = PedidoMesaViewModel(mesaId, repo)

    @Test
    fun alAbrir_TraeLaCartaAgrupadaYSinPedidoEsPedidoNuevo() {
        val s = vm().uiState.value
        assertFalse(s.cargando)
        assertEquals(listOf("Fondos", "Bebidas"), s.cartaPorCategoria.keys.toList())
        assertFalse(s.segundaRonda)
        assertFalse(s.puedeEnviar)
    }

    @Test
    fun sumarYRestar_CalculaTotalYNoBajaDeCero() {
        val vm = vm()
        vm.sumar(10); vm.sumar(10); vm.sumar(11)
        assertEquals(3, vm.uiState.value.totalPlatillos)
        assertEquals(0, BigDecimal("72.50").compareTo(vm.uiState.value.total))

        vm.restar(11); vm.restar(11)
        assertEquals(mapOf(10L to 2), vm.uiState.value.cantidades)
    }

    @Test
    fun sumar_NoPasaDelTope() {
        val vm = vm()
        repeat(PedidoMesaViewModel.MAXIMO_POR_PLATILLO + 5) { vm.sumar(10) }
        assertEquals(PedidoMesaViewModel.MAXIMO_POR_PLATILLO, vm.uiState.value.cantidades[10])
    }

    @Test
    fun enviar_Vacio_NoLlamaAlBackend() {
        vm().enviar()
        assertTrue(repo.enviados.isEmpty())
    }

    @Test
    fun enviar_MesaLibre_CreaElPedidoConLoElegido() {
        val vm = vm(mesaId = 1)
        vm.sumar(10); vm.sumar(11); vm.sumar(11)
        vm.enviar()

        assertEquals(FakeTomaPedidoRepository.Envio(1, null, mapOf(10L to 1, 11L to 2)), repo.enviados.single())
        assertTrue(vm.uiState.value.enviado)
        assertTrue(vm.uiState.value.cantidades.isEmpty())
    }

    @Test
    fun enviar_MandaLosPlatillosEnElOrdenDeLaCarta() {
        val vm = vm()
        vm.sumar(11); vm.sumar(10)
        vm.enviar()

        assertEquals(listOf(10L, 11L), repo.enviados.single().items.keys.toList())
    }

    @Test
    fun enviar_ConPedidoAbierto_EsSegundaRondaSobreEsePedido() {
        repo.pedidoDeMesa = Resultado.Exito(FakeTomaPedidoRepository.pedido(7))
        val vm = vm(mesaId = 2)
        assertTrue(vm.uiState.value.segundaRonda)
        assertEquals(1, vm.uiState.value.pedidoActual?.platillos?.size)

        vm.sumar(11)
        vm.enviar()

        assertEquals(7L, repo.enviados.single().pedidoId)
    }

    @Test
    fun enviar_SiFalla_MuestraElErrorYConservaLoElegido() {
        repo.respuesta = Resultado.Error("La mesa no está libre")
        val vm = vm()
        vm.sumar(10)
        vm.enviar()

        val s = vm.uiState.value
        assertEquals("La mesa no está libre", s.errorEnvio)
        assertFalse(s.enviado)
        assertFalse(s.enviando)
        assertEquals(mapOf(10L to 1), s.cantidades)

        vm.sumar(10)
        assertNull(vm.uiState.value.errorEnvio)
    }

    @Test
    fun siLaCartaNoCarga_MuestraElErrorYReintenta() {
        val bien = repo.carta
        repo.carta = Resultado.Error("No se pudo conectar con el catálogo")
        val vm = vm()
        assertEquals("No se pudo conectar con el catálogo", vm.uiState.value.errorCarga)

        repo.carta = bien
        vm.cargar()
        assertNull(vm.uiState.value.errorCarga)
        assertEquals(2, vm.uiState.value.carta.size)
    }
}
