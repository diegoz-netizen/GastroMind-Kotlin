package com.gastromind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.EstiloCifraGrande

const val LARGO_PIN = 4

/** Los 4 puntos del PIN: llenos los dígitos ya escritos. Nunca muestra los números. */
@Composable
fun PuntosPin(digitos: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.lg),
        modifier = modifier.semantics { contentDescription = "$digitos de $LARGO_PIN dígitos" }
    ) {
        repeat(LARGO_PIN) { i ->
            val lleno = i < digitos
            Box(
                Modifier
                    .size(Dimens.puntoPin)
                    .background(if (lleno) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, CircleShape)
                    .border(Dimens.xs / 2, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

/** Teclado numérico grande (sirve con guantes o manos ocupadas). */
@Composable
fun TecladoPin(
    onDigito: (Char) -> Unit,
    onBorrar: () -> Unit,
    habilitado: Boolean,
    modifier: Modifier = Modifier
) {
    val filas = listOf("123", "456", "789")
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md), modifier = modifier) {
        filas.forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                fila.forEach { d -> Tecla(d.toString(), { onDigito(d) }, habilitado) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
            Box(Modifier.size(Dimens.teclaPin))
            Tecla("0", { onDigito('0') }, habilitado)
            FilledTonalButton(
                onClick = onBorrar,
                enabled = habilitado,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.size(Dimens.teclaPin)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = "Borrar")
            }
        }
    }
}

@Composable
private fun Tecla(texto: String, onClick: () -> Unit, habilitado: Boolean) {
    FilledTonalButton(
        onClick = onClick,
        enabled = habilitado,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.size(Dimens.teclaPin)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(texto, style = EstiloCifraGrande)
        }
    }
}
