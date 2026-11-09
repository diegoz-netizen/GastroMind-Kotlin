package com.gastromind.app.ui.rol

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.ui.login.FakeAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InicioRolViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun cerrarSesionLlamaAlRepositorio() {
        val repo = FakeAuthRepository()
        val vm = InicioRolViewModel(repo)

        vm.cerrarSesion()

        assertEquals(1, repo.cierresDeSesion)
    }

    @Test
    fun cerrarSesionDosVecesLlamaDosVeces() {
        val repo = FakeAuthRepository()
        val vm = InicioRolViewModel(repo)

        vm.cerrarSesion()
        vm.cerrarSesion()

        assertEquals(2, repo.cierresDeSesion)
    }
}
