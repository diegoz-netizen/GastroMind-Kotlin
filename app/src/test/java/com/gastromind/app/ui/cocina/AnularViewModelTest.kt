package com.gastromind.app.ui.cocina

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.remote.dto.MotivoMermaDto
import com.gastromind.app.data.repository.AnularRepository
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.repository.Pedido
import com.gastromind.app.data.repository.Platillo
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.ui.pedidos.PlatilloComanda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

private class FakeAnularRepository : AnularRepository {
    var motivos: Resultado<List<MotivoMermaDto>> = Resultado.Exito(
        listOf(MotivoMermaDto(1, "VENCIDO"), MotivoMermaDto(8, "PLATO_ANULADO"), MotivoMermaDto(3, "QUEMADO"))
    )
    var respuesta: Resultado<Pedido> = Resultado.Exito(
        Pedido(7, 4, LocalDateTime.of(2026, 11, 7, 13, 0), listOf(Platillo(70, "Lomo saltado", 1, EstadoPlatillo.ANULADO)))
    )
    var lecturasMotivos = 0
    val anulados = mutableListOf<Triple<Long, Long, Long?>>()

    override suspend fun motivos(): Resultado<List<MotivoMermaDto>> {
        lecturasMotivos++
        return motivos
    }

    override suspend fun anular(pedidoId: Long, platilloId: Long, motivoId: Long?): Resultado<Pedido> {
        anulados += Triple(pedidoId, platilloId, motivoId)
        return respuesta
    }
}

class AnularViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakeAnularRepository()
    private val vm = AnularViewModel(repo)

    private fun platillo(estado: EstadoPlatillo) = PlatilloComanda(70, "Lomo saltado", 1, estado, null)

    @Test
    fun abrirUnPlatilloEnPreparacion_TraeLosMotivosYSugierePlatoAnulado() {
        vm.abrir(7, 4, platillo(EstadoPlatillo.EN_PREPARACION))

        val s = vm.uiState.value
        assertTrue(s.abierto)
        assertEquals(listOf(1L, 8L, 3L), s.motivos.map { it.id })
        assertEquals(8L, s.motivoId)
    }

    @Test
    fun loPendiente_NoSeAnulaEnCocina() {
        vm.abrir(7, 4, platillo(EstadoPlatillo.PENDIENTE))

        assertFalse(vm.uiState.value.abierto)
        assertEquals(0, repo.lecturasMotivos)
    }

    @Test
    fun confirmar_EnviaElMotivoElegidoYDevuelveElPedido() {
        var devuelto: Pedido? = null
        vm.abrir(7, 4, platillo(EstadoPlatillo.LISTO))
        vm.onMotivo(3)
        vm.confirmar { devuelto = it }

        assertEquals(listOf(Triple(7L, 70L, 3L as Long?)), repo.anulados)
        assertEquals(EstadoPlatillo.ANULADO, devuelto?.platillos?.single()?.estado)
        assertFalse(vm.uiState.value.abierto)
    }

    @Test
    fun sinMotivo_NoEnviaYLoPide() {
        repo.motivos = Resultado.Exito(listOf(MotivoMermaDto(1, "VENCIDO")))
        vm.abrir(7, 4, platillo(EstadoPlatillo.EN_PREPARACION))
        vm.confirmar { }

        assertTrue(repo.anulados.isEmpty())
        assertEquals("Elige el motivo.", vm.uiState.value.error)
    }

    @Test
    fun errorDelBackend_SeQuedaAbiertoConElMensaje() {
        repo.respuesta = Resultado.Error("Un platillo SERVIDO no se puede anular")
        var devuelto: Pedido? = null
        vm.abrir(7, 4, platillo(EstadoPlatillo.LISTO))
        vm.confirmar { devuelto = it }

        val s = vm.uiState.value
        assertTrue(s.abierto)
        assertFalse(s.guardando)
        assertEquals("Un platillo SERVIDO no se puede anular", s.error)
        assertNull(devuelto)
    }

    @Test
    fun losMotivosSeLeenUnaSolaVez() {
        vm.abrir(7, 4, platillo(EstadoPlatillo.EN_PREPARACION))
        vm.cerrar()
        vm.abrir(7, 4, platillo(EstadoPlatillo.LISTO))

        assertEquals(1, repo.lecturasMotivos)
        assertEquals(8L, vm.uiState.value.motivoId)
    }

    @Test
    fun nombreDeMotivo_SeLeeComoTexto() {
        assertEquals("Plato anulado", nombreDeMotivo("PLATO_ANULADO"))
        assertEquals("Devolución", nombreDeMotivo("DEVOLUCION"))
    }
}
