package com.gastromind.app.ui.pedidos

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.remote.dto.DetalleResponse
import com.gastromind.app.data.remote.dto.PedidoResponse
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.repository.EstadoPlatillo.EN_PREPARACION
import com.gastromind.app.data.repository.EstadoPlatillo.LISTO
import com.gastromind.app.data.repository.EstadoPlatillo.PENDIENTE
import com.gastromind.app.data.repository.EstadoPlatillo.SERVIDO
import com.gastromind.app.data.repository.Pedido
import com.gastromind.app.data.repository.PedidoRepository
import com.gastromind.app.data.repository.Platillo
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.data.repository.aPedido
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class PedidosEnVivoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val ahora = LocalDateTime.of(2026, 11, 1, 13, 18)

    private fun plato(id: Long, estado: EstadoPlatillo) = Platillo(id, "Plato $id", 1, estado)

    private fun pedido(id: Long, minutos: Long, vararg platillos: Platillo) =
        Pedido(id, id.toInt(), ahora.minusMinutes(minutos), platillos.toList())

    private class FakeRepo(var pedidos: List<Pedido> = emptyList()) : PedidoRepository {
        var lecturas = 0
        var errorLectura: String? = null
        var errorCambio: String? = null
        var lecturaEnEspera: CompletableDeferred<Unit>? = null
        val cambios = mutableListOf<Pair<Long, EstadoPlatillo>>()

        override suspend fun vigentes(): Resultado<List<Pedido>> {
            lecturas++
            // Lo que el backend tenía al momento de leer, aunque la respuesta llegue después
            val leidos = pedidos
            lecturaEnEspera?.await()
            return errorLectura?.let { Resultado.Error(it) } ?: Resultado.Exito(leidos)
        }

        override suspend fun cambiarEstado(pedidoId: Long, platilloId: Long, nuevo: EstadoPlatillo): Resultado<Pedido> {
            errorCambio?.let { return Resultado.Error(it) }
            cambios += platilloId to nuevo
            pedidos = pedidos.map { pedido ->
                if (pedido.id != pedidoId) pedido
                else pedido.copy(platillos = pedido.platillos.map { if (it.id == platilloId) it.copy(estado = nuevo) else it })
            }
            return Resultado.Exito(pedidos.first { it.id == pedidoId })
        }
    }

    private fun TestScope.iniciar(repo: FakeRepo, paso: (EstadoPlatillo) -> EstadoPlatillo? = pasoDeCocina): PedidosEnVivoViewModel {
        val vm = PedidosEnVivoViewModel(repo, paso) { ahora }
        backgroundScope.launch(mainRule.dispatcher) { vm.mantenerActualizado() }
        runCurrent()
        return vm
    }

    private fun PedidosEnVivoViewModel.platillo(id: Long) =
        uiState.value.comandas.flatMap { it.platillos }.first { it.id == id }

    private fun PedidosEnVivoViewModel.comanda(id: Long) =
        uiState.value.comandas.first { it.pedidoId == id }

    @Test
    fun alEntrar_MuestraLosPedidosConSuHoraYMinutos() = runTest(mainRule.dispatcher.scheduler) {
        val vm = iniciar(FakeRepo(listOf(pedido(1, 13, plato(1, PENDIENTE)))))

        val estado = vm.uiState.value
        assertFalse(estado.cargando)
        assertEquals(ahora, estado.ahora)
        assertEquals(13L, estado.comandas.single().minutos)
        assertEquals(ahora.minusMinutes(13), estado.comandas.single().horaIngreso)
    }

    @Test
    fun urgencia_SaleDeLosMinutosEnEspera() = runTest(mainRule.dispatcher.scheduler) {
        val vm = iniciar(FakeRepo(listOf(
            pedido(1, 1, plato(1, PENDIENTE)),
            pedido(2, 2, plato(2, PENDIENTE)),
            pedido(3, 9, plato(3, PENDIENTE)),
            pedido(4, 10, plato(4, PENDIENTE))
        )))

        assertEquals(Urgencia.NUEVA, vm.comanda(1).urgencia)
        assertEquals(Urgencia.NORMAL, vm.comanda(2).urgencia)
        assertEquals(Urgencia.NORMAL, vm.comanda(3).urgencia)
        assertEquals(Urgencia.DEMORADA, vm.comanda(4).urgencia)
    }

    @Test
    fun servidosYAnulados_NoSeMuestranYElPedidoTerminadoSale() = runTest(mainRule.dispatcher.scheduler) {
        val vm = iniciar(FakeRepo(listOf(
            pedido(1, 5, plato(1, LISTO), plato(2, SERVIDO), plato(3, EstadoPlatillo.ANULADO)),
            pedido(2, 5, plato(4, SERVIDO))
        )))

        val estado = vm.uiState.value
        assertEquals(listOf(1L), estado.comandas.map { it.pedidoId })
        assertEquals(listOf(1L), estado.comandas.single().platillos.map { it.id })
        assertEquals(1, estado.totalPlatillos)
    }

    @Test
    fun ca10_UnPedidoNuevoApareceEnMenosDe5Segundos() = runTest(mainRule.dispatcher.scheduler) {
        val repo = FakeRepo()
        val vm = iniciar(repo)
        assertTrue(vm.uiState.value.comandas.isEmpty())

        repo.pedidos = listOf(pedido(1, 0, plato(1, PENDIENTE)))
        advanceTimeBy(4_999)

        assertEquals(1, vm.uiState.value.comandas.size)
    }

    @Test
    fun cocina_AvanzaDePendienteAPreparandoYAListoYAhiSeDetiene() = runTest(mainRule.dispatcher.scheduler) {
        val repo = FakeRepo(listOf(pedido(1, 5, plato(1, PENDIENTE))))
        val vm = iniciar(repo)

        vm.avanzar(1, vm.platillo(1))
        assertEquals(EN_PREPARACION, vm.platillo(1).estado)
        vm.avanzar(1, vm.platillo(1))
        assertEquals(LISTO, vm.platillo(1).estado)
        assertNull(vm.platillo(1).siguiente)
        vm.avanzar(1, vm.platillo(1))

        assertEquals(listOf(1L to EN_PREPARACION, 1L to LISTO), repo.cambios)
    }

    @Test
    fun ca11_ElMozoVeElCambioDeCocinaYSoloPuedeServirLoListo() = runTest(mainRule.dispatcher.scheduler) {
        val repo = FakeRepo(listOf(pedido(1, 5, plato(1, EN_PREPARACION))))
        val vm = iniciar(repo, pasoDeMozo)
        assertNull(vm.platillo(1).siguiente)

        // Cocina lo termina en otro dispositivo
        repo.pedidos = listOf(pedido(1, 5, plato(1, LISTO)))
        advanceTimeBy(INTERVALO_REFRESCO_MS + 1)
        assertEquals(SERVIDO, vm.platillo(1).siguiente)

        vm.avanzar(1, vm.platillo(1))

        assertEquals(listOf(1L to SERVIDO), repo.cambios)
        assertTrue(vm.uiState.value.comandas.isEmpty())
    }

    @Test
    fun sinConexion_ConservaLoUltimoYAvisaHastaQueVuelve() = runTest(mainRule.dispatcher.scheduler) {
        val repo = FakeRepo(listOf(pedido(1, 5, plato(1, PENDIENTE))))
        val vm = iniciar(repo)

        repo.errorLectura = "Sin conexión"
        advanceTimeBy(INTERVALO_REFRESCO_MS + 1)
        assertEquals("Sin conexión", vm.uiState.value.error)
        assertEquals(1, vm.uiState.value.comandas.size)

        repo.errorLectura = null
        advanceTimeBy(INTERVALO_REFRESCO_MS)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun cambioRechazado_AvisaYTraeElEstadoActual() = runTest(mainRule.dispatcher.scheduler) {
        val repo = FakeRepo(listOf(pedido(1, 5, plato(1, PENDIENTE))))
        val vm = iniciar(repo)
        val leido = vm.platillo(1)

        // Otra tablet ya lo empezó: el backend responde 409
        repo.pedidos = listOf(pedido(1, 5, plato(1, EN_PREPARACION)))
        repo.errorCambio = "Un platillo EN_PREPARACION no puede pasar a EN_PREPARACION"
        vm.avanzar(1, leido)

        assertEquals(repo.errorCambio, vm.uiState.value.error)
        assertEquals(EN_PREPARACION, vm.platillo(1).estado)
        assertTrue(vm.uiState.value.cambiando.isEmpty())

        // El refresco siguiente no borra el aviso; lo borra la siguiente acción
        advanceTimeBy(INTERVALO_REFRESCO_MS + 1)
        assertEquals(repo.errorCambio, vm.uiState.value.error)
    }

    @Test
    fun lecturaQueEmpezoAntesDelCambio_NoPisaElEstadoNuevo() = runTest(mainRule.dispatcher.scheduler) {
        val repo = FakeRepo(listOf(pedido(1, 5, plato(1, PENDIENTE))))
        val vm = iniciar(repo)

        val demora = CompletableDeferred<Unit>()
        repo.lecturaEnEspera = demora
        advanceTimeBy(INTERVALO_REFRESCO_MS + 1)
        assertEquals(2, repo.lecturas)

        vm.avanzar(1, vm.platillo(1))
        repo.lecturaEnEspera = null
        demora.complete(Unit)
        runCurrent()

        assertEquals(EN_PREPARACION, vm.platillo(1).estado)
    }

    @Test
    fun respuestaDelBackend_SeConvierteEnPedido() {
        val pedido = PedidoResponse(
            7, 4, "2026-11-01T12:47:00.123456",
            listOf(DetalleResponse(1, "Lomo saltado", 2, "EN_PREPARACION"), DetalleResponse(2, "Chicha", 1, "OTRO"))
        ).aPedido(ZoneId.of("America/Lima"))

        // El backend manda UTC: en Lima son 5 horas menos
        assertEquals(LocalDateTime.of(2026, 11, 1, 7, 47, 0, 123_456_000), pedido.fechaRegistro)
        assertEquals(EN_PREPARACION, pedido.platillos[0].estado)
        assertEquals(EstadoPlatillo.DESCONOCIDO, pedido.platillos[1].estado)
    }
}
