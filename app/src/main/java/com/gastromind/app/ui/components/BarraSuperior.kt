package com.gastromind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Barra de todas las pantallas de rol, con el diseño móvil v3 (Victor): arriba la marca y quién está,
 * en verde y mayúsculas; debajo el título grande de la sección; a la derecha las iniciales y cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    subtitulo: String,
    onCerrarSesion: () -> Unit,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "GASTROMIND · ${subtitulo.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Normal)
                }
            },
            actions = {
                acciones()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Text(
                        iniciales(subtitulo),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onCerrarSesion) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Cerrar sesión")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** "Mozo Sede Central · 2 platillos" → "MS" (rol y sede, como el avatar del prototipo). */
private fun iniciales(subtitulo: String): String =
    subtitulo.substringBefore('·').split(' ').filter { it.isNotBlank() && it[0].isUpperCase() }
        .let { palabras -> listOfNotNull(palabras.firstOrNull(), palabras.lastOrNull()) }
        .joinToString("") { it.first().toString() }
