package com.gastromind.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Fecha opcional: se elige en un calendario y se puede quitar. [desde] limita los días que se pueden elegir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onFecha: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    desde: LocalDate? = null
) {
    var abierto by rememberSaveable { mutableStateOf(false) }

    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = fecha?.format(FORMATO_FECHA).orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(etiqueta) },
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Un campo de solo lectura se queda con el toque: esta capa es la que abre el calendario
        val texto = fecha?.format(FORMATO_FECHA)
        Box(
            Modifier
                .matchParentSize()
                .clickable(onClickLabel = "Elegir la fecha") { abierto = true }
                .semantics { contentDescription = if (texto != null) "$etiqueta: $texto" else etiqueta }
        )
    }
    if (fecha != null) {
        BotonTexto("Quitar la fecha", onClick = { onFecha(null) })
    }

    if (abierto) {
        // El DatePicker trabaja en milisegundos UTC: con otra zona, la fecha se correría un día
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = fecha?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val dia = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return desde == null || !dia.isBefore(desde)
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                BotonTexto("Aceptar", onClick = {
                    estado.selectedDateMillis?.let {
                        onFecha(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    abierto = false
                })
            },
            dismissButton = { BotonTexto("Cancelar", onClick = { abierto = false }) }
        ) {
            DatePicker(state = estado)
        }
    }
}
