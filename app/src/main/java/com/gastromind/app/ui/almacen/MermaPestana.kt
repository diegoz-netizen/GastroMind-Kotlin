package com.gastromind.app.ui.almacen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gastromind.app.data.remote.dto.MermaResponse
import com.gastromind.app.ui.cocina.nombreDeMotivo
import com.gastromind.app.ui.components.BotonPrincipal
import com.gastromind.app.ui.components.CampoSeleccion
import com.gastromind.app.ui.components.CampoTexto
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import java.math.RoundingMode

/** HU-17: el almacenero registra lo que se perdió de un insumo y por qué. Descuenta el stock. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MermaPestana(
    estado: MermaUiState,
    onReintentar: () -> Unit,
    onInsumo: (Long) -> Unit,
    onCantidad: (String) -> Unit,
    onMotivo: (Long) -> Unit,
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
            Text("Registrar merma", style = MaterialTheme.typography.titleLarge)
            Text(
                "Lo que se perdió (vencido, mal manejo, derramado...). Se descuenta del stock y queda a tu nombre.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CampoSeleccion(
                etiqueta = "Insumo",
                opciones = estado.insumos,
                seleccion = estado.insumo,
                texto = { "${it.nombre} · quedan ${cantidadConFormato(it.stockActual)} ${it.unidadMedida}" },
                onSeleccion = { onInsumo(it.id) }
            )
            CampoTexto(
                valor = estado.cantidad,
                onCambio = onCantidad,
                etiqueta = estado.insumo?.let { "Cantidad perdida (${it.unidadMedida})" } ?: "Cantidad perdida",
                tipoTeclado = KeyboardType.Decimal,
                accion = ImeAction.Done
            )
            Text("Motivo", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                estado.motivos.forEach { motivo ->
                    FilterChip(
                        selected = motivo.id == estado.motivoId,
                        onClick = { onMotivo(motivo.id) },
                        label = { Text(nombreDeMotivo(motivo.nombre)) }
                    )
                }
            }
            estado.error?.let { MensajeError(it) }
            BotonPrincipal("Registrar merma", onClick = onRegistrar, cargando = estado.guardando)
            estado.ultima?.let { ResultadoMerma(it) }
        }
    }
}

@Composable
private fun ResultadoMerma(merma: MermaResponse) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs), modifier = Modifier.padding(Dimens.lg)) {
            Text("Merma registrada · ${merma.insumo}", style = MaterialTheme.typography.titleMedium)
            Text(nombreDeMotivo(merma.motivo), style = MaterialTheme.typography.bodyLarge)
            Linea("Perdido", "${cantidadConFormato(merma.cantidad)} ${merma.unidadMedida}")
            Linea("Quedan", "${cantidadConFormato(merma.stockDespues)} ${merma.unidadMedida}")
            // Soles con coma, como en el kardex y la cuenta del mozo
            Linea("Costo", "S/ " + merma.costo.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ','))
        }
    }
}

@Composable
private fun Linea(etiqueta: String, valor: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontFamily = FuenteCifras)
    }
}
