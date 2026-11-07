package com.gastromind.app.ui.almacen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gastromind.app.data.remote.dto.ConteoResponse
import com.gastromind.app.ui.components.BotonPrincipal
import com.gastromind.app.ui.components.CampoSeleccion
import com.gastromind.app.ui.components.CampoTexto
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import java.math.BigDecimal

/** HU-30: el almacenero cuenta un insumo y ve si falta o sobra contra el sistema. El stock no cambia. */
@Composable
fun ConteoPestana(
    estado: ConteoUiState,
    onReintentar: () -> Unit,
    onInsumo: (Long) -> Unit,
    onCantidad: (String) -> Unit,
    onRegistrar: () -> Unit
) {
    when {
        estado.cargando -> PantallaCarga()
        estado.errorCarga != null -> Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.lg),
            modifier = Modifier.widthIn(max = Dimens.anchoFormulario).padding(Dimens.lg)
        ) {
            MensajeError(estado.errorCarga)
            BotonPrincipal("Reintentar", onClick = onReintentar)
        }
        else -> Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
            modifier = Modifier
                .widthIn(max = Dimens.anchoFormulario)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.lg)
        ) {
            Text("Conteo físico", style = MaterialTheme.typography.titleLarge)
            Text(
                "Cuenta lo que hay en el almacén. La diferencia con el sistema queda registrada; el stock no cambia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CampoSeleccion(
                etiqueta = "Insumo",
                opciones = estado.insumos,
                seleccion = estado.insumo,
                texto = { "${it.nombre} · sistema ${cantidadConFormato(it.stockActual)} ${it.unidadMedida}" },
                onSeleccion = { onInsumo(it.id) }
            )
            CampoTexto(
                valor = estado.cantidad,
                onCambio = onCantidad,
                etiqueta = estado.insumo?.let { "Cantidad contada (${it.unidadMedida})" } ?: "Cantidad contada",
                tipoTeclado = KeyboardType.Decimal,
                accion = ImeAction.Done
            )
            estado.error?.let { MensajeError(it) }
            BotonPrincipal("Registrar conteo", onClick = onRegistrar, cargando = estado.guardando)
            estado.ultimo?.let { ResultadoConteo(it) }
        }
    }
}

@Composable
private fun ResultadoConteo(conteo: ConteoResponse) {
    val signo = conteo.diferencia.signum()
    val (fondo, texto) = when {
        signo < 0 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        signo > 0 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    val unidad = conteo.unidadMedida
    val resumen = when {
        signo < 0 -> "Faltan ${cantidadConFormato(conteo.diferencia.abs())} $unidad"
        signo > 0 -> "Sobran ${cantidadConFormato(conteo.diferencia)} $unidad"
        else -> "Coincide con el sistema"
    }
    Surface(color = fondo, contentColor = texto, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs), modifier = Modifier.padding(Dimens.lg)) {
            Text("Conteo registrado · ${conteo.insumo}", style = MaterialTheme.typography.titleMedium)
            Text(resumen, style = MaterialTheme.typography.bodyLarge)
            Linea("Contado", conteo.cantidadContada, unidad)
            Linea("Sistema", conteo.stockSistema, unidad)
        }
    }
}

@Composable
private fun Linea(etiqueta: String, valor: BigDecimal, unidad: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text("${cantidadConFormato(valor)} $unidad", style = MaterialTheme.typography.bodyMedium, fontFamily = FuenteCifras)
    }
}
