package com.gastromind.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gastromind.app.data.session.EmpleadoRecordado
import com.gastromind.app.ui.components.AvatarIniciales
import com.gastromind.app.ui.components.BotonPrincipal
import com.gastromind.app.ui.components.BotonTexto
import com.gastromind.app.ui.components.CampoTexto
import com.gastromind.app.ui.components.Marca
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PuntosPin
import com.gastromind.app.ui.components.TecladoPin
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.GastroMindTheme

@Composable
fun LoginRoute(vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory)) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        state = state,
        onCorreo = vm::onCorreo,
        onPassword = vm::onPassword,
        onDigito = vm::onDigito,
        onBorrar = vm::onBorrar,
        onModo = vm::onModo,
        onNoSoyYo = { vm.onNoSoyYo() },
        onEntrarConCorreo = vm::entrarConCorreo
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onCorreo: (String) -> Unit,
    onPassword: (String) -> Unit,
    onDigito: (Char) -> Unit,
    onBorrar: () -> Unit,
    onModo: (ModoIngreso) -> Unit,
    onNoSoyYo: () -> Unit,
    onEntrarConCorreo: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        // Arriba y no centrado: si apareciera o se fuera el mensaje de error, un contenido centrado se movería
        // y el siguiente toque caería fuera de la tecla. El error va al final, debajo del teclado.
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.xl),
                modifier = Modifier.widthIn(max = Dimens.anchoFormulario).fillMaxWidth()
                    .padding(horizontal = Dimens.xl, vertical = Dimens.xxl)
            ) {
                Marca(lema = "Personal del restaurante")
                when (state.modo) {
                    ModoIngreso.PIN -> IngresoConPin(state, onCorreo, onDigito, onBorrar, onNoSoyYo)
                    ModoIngreso.CORREO -> IngresoConCorreo(state, onCorreo, onPassword, onEntrarConCorreo)
                }
                state.error?.let { MensajeError(it) }
                BotonTexto(
                    texto = if (state.modo == ModoIngreso.PIN) "Entrar con correo y contraseña" else "Entrar con mi PIN",
                    onClick = { onModo(if (state.modo == ModoIngreso.PIN) ModoIngreso.CORREO else ModoIngreso.PIN) }
                )
            }
        }
    }
}

@Composable
private fun IngresoConPin(
    state: LoginUiState,
    onCorreo: (String) -> Unit,
    onDigito: (Char) -> Unit,
    onBorrar: () -> Unit,
    onNoSoyYo: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.lg)) {
        val recordado = state.recordado
        if (recordado != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                AvatarIniciales(recordado.nombreCompleto)
                Column {
                    Text(recordado.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                    Text(recordado.correo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            BotonTexto("¿No eres tú?", onNoSoyYo)
        } else {
            CampoTexto(
                valor = state.correo,
                onCambio = onCorreo,
                etiqueta = "Correo",
                tipoTeclado = KeyboardType.Email,
                accion = ImeAction.Done,
                habilitado = !state.cargando
            )
        }
        Text("Ingresa tu PIN", style = MaterialTheme.typography.titleMedium)
        PuntosPin(digitos = state.pin.length)
        TecladoPin(onDigito = onDigito, onBorrar = onBorrar, habilitado = !state.cargando)
    }
}

@Composable
private fun IngresoConCorreo(
    state: LoginUiState,
    onCorreo: (String) -> Unit,
    onPassword: (String) -> Unit,
    onEntrar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        CampoTexto(
            valor = state.correo,
            onCambio = onCorreo,
            etiqueta = "Correo",
            tipoTeclado = KeyboardType.Email,
            habilitado = !state.cargando
        )
        CampoTexto(
            valor = state.password,
            onCambio = onPassword,
            etiqueta = "Contraseña",
            tipoTeclado = KeyboardType.Password,
            accion = ImeAction.Done,
            onAccion = onEntrar,
            esSecreto = true,
            habilitado = !state.cargando
        )
        BotonPrincipal("Entrar", onEntrar, cargando = state.cargando)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun LoginPinPreview() {
    GastroMindTheme {
        LoginScreen(
            state = LoginUiState(recordado = EmpleadoRecordado("cocina.central@gastromind.demo", "Cocinero Sede Central"), pin = "12"),
            onCorreo = {}, onPassword = {}, onDigito = {}, onBorrar = {}, onModo = {}, onNoSoyYo = {}, onEntrarConCorreo = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun LoginCorreoPreview() {
    GastroMindTheme {
        LoginScreen(
            state = LoginUiState(modo = ModoIngreso.CORREO, correo = "mozo.central@gastromind.demo", error = "Credenciales inválidas"),
            onCorreo = {}, onPassword = {}, onDigito = {}, onBorrar = {}, onModo = {}, onNoSoyYo = {}, onEntrarConCorreo = {}
        )
    }
}
