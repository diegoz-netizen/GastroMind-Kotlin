package com.gastromind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gastromind.app.ui.theme.Dimens

/** Una sección de la barra inferior: texto e ícono. */
data class Seccion(val texto: String, val icono: ImageVector)

/** Barra inferior del diseño móvil v3: reemplaza a las pestañas de arriba en las pantallas de rol. */
@Composable
fun BarraInferior(secciones: List<Seccion>, seleccion: Int, onSeleccion: (Int) -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
            secciones.forEachIndexed { i, seccion ->
                NavigationBarItem(
                    selected = i == seleccion,
                    onClick = { onSeleccion(i) },
                    icon = { Icon(seccion.icono, contentDescription = null) },
                    label = { Text(seccion.texto, style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

/** Título de sección del v3: una línea verde en mayúsculas y el título debajo. */
@Composable
fun TituloSeccion(eyebrow: String, titulo: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(titulo, style = MaterialTheme.typography.titleLarge)
    }
}

/** Cuadro con el número de mesa ("M3") que encabeza las comandas, en cocina y en el salón. */
@Composable
fun InsigniaMesa(numero: Int, fondo: Color = MaterialTheme.colorScheme.primary, texto: Color = MaterialTheme.colorScheme.onPrimary) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(Dimens.xxl + Dimens.sm).background(fondo, MaterialTheme.shapes.small)
    ) {
        Text("M$numero", style = MaterialTheme.typography.titleSmall, color = texto)
    }
}

/** Pie de la comanda: "2 de 3 listos" y la barra de avance. */
@Composable
fun ProgresoListos(listos: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            "$listos de $total listos",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else listos.toFloat() / total },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
            gapSize = Dimens.none,
            drawStopIndicator = {},
            modifier = Modifier.weight(1f).height(Dimens.sm)
        )
    }
}
