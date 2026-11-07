package com.gastromind.app.ui.mozo

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.repository.Mesa
import com.gastromind.app.data.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class MesasViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakeTomaPedidoRepository()

    @Test
    fun cargar_TraeLasMesasDelSalon() {
        val vm = MesasViewModel(repo)
        vm.cargar()
        assertFalse(vm.uiState.value.cargando)
        assertEquals(listOf(true, false), vm.uiState.value.mesas.map { it.libre })
    }

    @Test
    fun siFallaAlRefrescar_SeQuedanLasMesasQueHabiaYMuestraElError() {
        val vm = MesasViewModel(repo)
        vm.cargar()
        repo.mesas = Resultado.Error("No se pudo conectar")
        vm.cargar()

        assertEquals(2, vm.uiState.value.mesas.size)
        assertEquals("No se pudo conectar", vm.uiState.value.error)

        repo.mesas = Resultado.Exito(listOf(Mesa(1, 1, false)))
        vm.cargar()
        assertEquals(null, vm.uiState.value.error)
    }
}
