package com.gastromind.app.ui.login

import com.gastromind.app.MainDispatcherRule
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.data.session.EmpleadoRecordado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val ana = EmpleadoRecordado("ana@test.com", "Ana Mozo")

    private fun escribirPin(vm: LoginViewModel, pin: String) = pin.forEach(vm::onDigito)

    @Test
    fun conEmpleadoRecordado_ElCuartoDigitoEntraConSuCorreo() {
        val repo = FakeAuthRepository(ana)
        val vm = LoginViewModel(repo)

        escribirPin(vm, "482")
        assertTrue(repo.intentosPin.isEmpty())
        vm.onDigito('1')

        assertEquals(listOf("ana@test.com" to "4821"), repo.intentosPin)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun pinIncorrecto_MuestraElMensajeYBorraElPin() {
        val repo = FakeAuthRepository(ana).apply { respuesta = Resultado.Error("Credenciales inválidas") }
        val vm = LoginViewModel(repo)

        escribirPin(vm, "0000")

        assertEquals("Credenciales inválidas", vm.uiState.value.error)
        assertEquals("", vm.uiState.value.pin)
    }

    @Test
    fun sinCorreo_NoLlamaAlBackend() {
        val repo = FakeAuthRepository()
        val vm = LoginViewModel(repo)

        escribirPin(vm, "4821")

        assertTrue(repo.intentosPin.isEmpty())
        assertEquals("Escribe tu correo antes del PIN.", vm.uiState.value.error)
    }

    @Test
    fun sinRecordado_UsaElCorreoEscrito() {
        val repo = FakeAuthRepository()
        val vm = LoginViewModel(repo)

        vm.onCorreo(" luis@test.com ")
        escribirPin(vm, "1234")

        assertEquals(listOf("luis@test.com" to "1234"), repo.intentosPin)
    }

    @Test
    fun noAceptaMasDe4DigitosNiLetras() {
        val repo = FakeAuthRepository(ana).apply { respuesta = Resultado.Error("x") }
        val vm = LoginViewModel(repo)

        vm.onDigito('a')
        escribirPin(vm, "12")
        vm.onBorrar()

        assertEquals("1", vm.uiState.value.pin)
    }

    @Test
    fun noSoyYo_OlvidaAlEmpleadoYPideElCorreo() {
        val repo = FakeAuthRepository(ana)
        val vm = LoginViewModel(repo)

        vm.onNoSoyYo()

        assertNull(vm.uiState.value.recordado)
        assertEquals("", vm.uiState.value.correoParaPin)
    }

    @Test
    fun conCorreo_ValidaAntesDeLlamar() {
        val repo = FakeAuthRepository()
        val vm = LoginViewModel(repo)
        vm.onModo(ModoIngreso.CORREO)

        vm.entrarConCorreo()
        assertEquals("Escribe un correo válido.", vm.uiState.value.error)

        vm.onCorreo("ana@test.com")
        vm.entrarConCorreo()
        assertEquals("Escribe tu contraseña.", vm.uiState.value.error)
        assertTrue(repo.intentosCorreo.isEmpty())

        vm.onPassword("Clave2026!")
        vm.entrarConCorreo()
        assertEquals(listOf("ana@test.com" to "Clave2026!"), repo.intentosCorreo)
        assertEquals("", vm.uiState.value.password)
    }

    @Test
    fun cambiarDeModo_LimpiaPinYError() {
        val repo = FakeAuthRepository(ana).apply { respuesta = Resultado.Error("Credenciales inválidas") }
        val vm = LoginViewModel(repo)
        escribirPin(vm, "0000")

        vm.onModo(ModoIngreso.CORREO)

        assertNull(vm.uiState.value.error)
        assertEquals(ModoIngreso.CORREO, vm.uiState.value.modo)
    }
}
