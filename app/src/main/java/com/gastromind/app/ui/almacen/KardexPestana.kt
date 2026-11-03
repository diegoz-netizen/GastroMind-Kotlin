package com.gastromind.app.ui.almacen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gastromind.app.data.remote.dto.MovimientoKardex
import com.gastromind.app.ui.components.BotonPrincipal
import com.gastromind.app.ui.components.CampoFecha
import com.gastromind.app.ui.components.CampoSeleccion
import com.gastromind.app.ui.components.EstadoVacio
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import java.time.LocalDate

@Composable
fun KardexPestana(
    estado: KardexUiState,
    onReintentar: () -> Unit,
    onInsumo: (Long) -> Unit,
    onDesde: (LocalDate?) -> Unit,
    onHasta: (LocalDate?) -> Unit
) {
    when {
        estado.cargandoInsumos -> PantallaCarga()
        estado.errorInsumos != null -> Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.lg),
            modifier = Modifier.widthIn(max = Dimens.anchoFormulario).padding(Dimens.lg)
        ) {
            MensajeError(estado.errorInsumos)
            BotonPrincipal("Reintentar", onClick = onReintentar)
        }
        else -> LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
            modifier = Modifier.widthIn(max = Dimens.anchoContenido).fillMaxSize().padding(horizontal = Dimens.lg)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                    modifier = Modifier.padding(top = Dimens.lg)
                ) {
                    CampoSeleccion(
                        etiqueta = "Insumo",
                        opciones = estado.insumos,
                        seleccion = estado.insumo,
                        texto = { "${it.nombre} · quedan ${cantidadConFormato(it.stockActual)} ${it.unidadMedida}" },
                        onSeleccion = { onInsumo(it.id) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                        Column(Modifier.weight(1f)) {
                            CampoFecha(etiqueta = "Desde", fecha = estado.desde, onFecha = onDesde)
                        }
                        Column(Modifier.weight(1f)) {
                            CampoFecha(etiqueta = "Hasta", fecha = estado.hasta, onFecha = onHasta)
                        }
                    }
                    estado.error?.let { MensajeError(it) }
                }
            }
            when {
                estado.insumo == null -> item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EstadoVacio(
                            icono = Icons.Outlined.Inventory2,
                            titulo = "Elige un insumo",
                            texto = "Aquí se ven sus ingresos, ventas y mermas, con el saldo de cada movimiento."
                        )
                    }
                }
                estado.cargandoMovimientos && estado.movimientos.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(Dimens.xl), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                estado.movimientos.isEmpty() && estado.error == null -> item {
                    Text(
                        "No hay movimientos en estas fechas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(Dimens.xl)
                    )
                }
                estado.movimientos.isNotEmpty() -> {
                    item { CabeceraKardex(estado.insumo?.unidadMedida.orEmpty()) }
                    items(estado.movimientos, key = { it.id }) { movimiento ->
                        FilaKardex(movimiento, esUltima = movimiento == estado.movimientos.last())
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    item { Box(Modifier.padding(bottom = Dimens.lg)) }
                }
            }
        }
    }
}

/** La unidad va en la cabecera: sin ella, las cifras se leen como plata (Planning 3, punto 8). */
@Composable
private fun CabeceraKardex(unidad: String) {
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
        TextoCabecera("Movimiento", Modifier.weight(PESO_DETALLE), TextAlign.Start)
        TextoCabecera("Entrada ($unidad)", Modifier.weight(PESO_CIFRA), TextAlign.End)
        TextoCabecera("Salida ($unidad)", Modifier.weight(PESO_CIFRA), TextAlign.End)
        TextoCabecera("Saldo ($unidad)", Modifier.weight(PESO_CIFRA), TextAlign.End)
    }
}

@Composable
private fun TextoCabecera(texto: String, modifier: Modifier, alineado: TextAlign) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = alineado,
        modifier = modifier
    )
}

@Composable
private fun FilaKardex(movimiento: MovimientoKardex, esUltima: Boolean) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.sm)
    ) {
        Column(Modifier.weight(PESO_DETALLE)) {
            Text(tituloDe(movimiento), style = MaterialTheme.typography.bodyMedium)
            Text(
                listOfNotNull(diaDe(movimiento), detalleDe(movimiento)).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Cifra(entradaConFormato(movimiento.entrada), MaterialTheme.colorScheme.primary, Modifier.weight(PESO_CIFRA))
        // Las mermas en rojo, como en el prototipo: son pérdida, no venta
        Cifra(
            salidaConFormato(movimiento.salida),
            if (movimiento.tipo == "MERMA") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            Modifier.weight(PESO_CIFRA)
        )
        Cifra(
            cantidadConFormato(movimiento.saldo),
            MaterialTheme.colorScheme.onSurface,
            Modifier.weight(PESO_CIFRA),
            negrita = esUltima
        )
    }
}

@Composable
private fun Cifra(texto: String, color: Color, modifier: Modifier, negrita: Boolean = false) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FuenteCifras,
        fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

private const val PESO_DETALLE = 1.6f
private const val PESO_CIFRA = 1f
