package com.gastromind.app.ui.mozo

import com.gastromind.app.ui.components.Tono
import com.gastromind.app.ui.components.ChipEstado
import com.gastromind.app.ui.components.ProgresoListos
import com.gastromind.app.ui.components.InsigniaMesa
import com.gastromind.app.ui.components.TituloSeccion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RoomService
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.ui.components.BarraSuperior
import com.gastromind.app.ui.components.EstadoVacio
import com.gastromind.app.ui.components.EtiquetaEstado
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.pedidos.Comanda
import com.gastromind.app.ui.pedidos.PedidosUiState
import com.gastromind.app.ui.pedidos.PlatilloComanda
import com.gastromind.app.ui.pedidos.TextoPlatillo
import com.gastromind.app.ui.pedidos.Urgencia
import com.gastromind.app.ui.pedidos.hora
import com.gastromind.app.ui.pedidos.pasoDeMozo
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.GastroMindTheme
import java.time.LocalDateTime

/**
 * HU-11 del lado del mozo (CA-11): ve cómo avanza cada platillo y marca lo que lleva a la mesa.
 * Es la sección "En cocina" del prototipo; la HU-09 la pone dentro de la pantalla de la mesa.
 */
@Composable
fun EnCocinaScreen(
    nombre: String,
    estado: PedidosUiState,
    onServir: (Long, PlatilloComanda) -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = "Salón", subtitulo = nombre, onCerrarSesion = onCerrarSesion) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        EnCocinaContenido(estado, onServir, Modifier.padding(padding))
    }
}

/** El cuerpo de "En cocina": lo usa esta pantalla y la pestaña del Salón (HU-09). */
@Composable
fun EnCocinaContenido(estado: PedidosUiState, onServir: (Long, PlatilloComanda) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        estado.error?.let { MensajeError(it, Modifier.padding(horizontal = Dimens.lg, vertical = Dimens.sm)) }
        when {
            estado.cargando -> PantallaCarga()
            estado.comandas.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EstadoVacio(
                    icono = Icons.Outlined.RoomService,
                    titulo = "Nada en cocina",
                    texto = "Aquí ves cómo avanza cada platillo y marcas lo que llevas a la mesa.",
                    etiqueta = "Cierre de mesa en construcción · HU-12 · Piero"
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
                modifier = Modifier.widthIn(max = Dimens.anchoContenido).fillMaxSize()
            ) {
                item { TituloSeccion("Seguimiento en vivo", "Pedidos del salón") }
                items(estado.comandas, key = { it.pedidoId }) { comanda ->
                    TarjetaMesa(comanda, estado.cambiando, onServir)
                }
            }
        }
    }
}

@Composable
private fun TarjetaMesa(comanda: Comanda, cambiando: Set<Long>, onServir: (Long, PlatilloComanda) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(Dimens.borde, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm), modifier = Modifier.padding(Dimens.md)) {
            // Encabezado del diseño móvil v3: insignia de la mesa, hora y estado
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                InsigniaMesa(comanda.mesaNumero)
                Column(Modifier.weight(1f)) {
                    Text("Mesa ${comanda.mesaNumero}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "desde ${comanda.horaIngreso.hora()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val hayListos = comanda.platillos.any { it.siguiente != null }
                ChipEstado(if (hayListos) "Para llevar" else "En cocina", if (hayListos) Tono.Ok else Tono.Aviso)
            }
            comanda.platillos.forEach { platillo ->
                FilaEnCocina(platillo, platillo.id in cambiando) { onServir(comanda.pedidoId, platillo) }
            }
            val activos = comanda.platillos.filter { it.estado != EstadoPlatillo.ANULADO }
            ProgresoListos(activos.count { it.estado == EstadoPlatillo.LISTO || it.estado == EstadoPlatillo.SERVIDO }, activos.size)
        }
    }
}

@Composable
private fun FilaEnCocina(platillo: PlatilloComanda, ocupado: Boolean, onServir: () -> Unit) {
    val listo = platillo.siguiente != null
    val fondo = if (listo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .background(fondo, MaterialTheme.shapes.small)
            .heightIn(min = Dimens.altoBotonEstado)
            .padding(horizontal = Dimens.md, vertical = Dimens.xs)
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            TextoPlatillo(platillo, Modifier.weight(1f))
        }
        when {
            listo -> Button(
                onClick = onServir,
                enabled = !ocupado,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.heightIn(min = Dimens.altoBotonEstado)
            ) {
                Text("Listo · servir", style = MaterialTheme.typography.labelLarge)
            }
            platillo.estado == EstadoPlatillo.EN_PREPARACION -> EtiquetaEstado("En preparación")
            else -> EtiquetaEstado(
                "Pendiente",
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun EnCocinaPreview() {
    val ahora = LocalDateTime.of(2026, 11, 1, 13, 18)
    fun plato(id: Long, nombre: String, cantidad: Int, estado: EstadoPlatillo) =
        PlatilloComanda(id, nombre, cantidad, estado, pasoDeMozo(estado))
    GastroMindTheme {
        EnCocinaScreen(
            nombre = "Mozo Sede Central",
            estado = PedidosUiState(
                cargando = false,
                ahora = ahora,
                comandas = listOf(
                    Comanda(1, 4, ahora.minusMinutes(13), 13, Urgencia.DEMORADA, listOf(
                        plato(1, "Lomo saltado", 2, EstadoPlatillo.LISTO),
                        plato(2, "Ceviche clásico", 1, EstadoPlatillo.EN_PREPARACION),
                        plato(3, "Chicha morada", 2, EstadoPlatillo.PENDIENTE)
                    ))
                )
            ),
            onServir = { _, _ -> },
            onCerrarSesion = {}
        )
    }
}
