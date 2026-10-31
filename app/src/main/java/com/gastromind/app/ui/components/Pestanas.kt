package com.gastromind.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.gastromind.app.ui.theme.Dimens

/** Pestañas del prototipo: un fondo gris con la opción elegida en blanco. */
@Composable
fun Pestanas(opciones: List<String>, seleccion: Int, onSeleccion: (Int) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(Dimens.xs)) {
            opciones.forEachIndexed { indice, texto ->
                val elegida = indice == seleccion
                Surface(
                    color = if (elegida) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(Dimens.altoPestana)
                            .clickable { onSeleccion(indice) }
                            .semantics {
                                role = Role.Tab
                                selected = elegida
                            }
                    ) {
                        Text(
                            texto,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (elegida) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (elegida) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
