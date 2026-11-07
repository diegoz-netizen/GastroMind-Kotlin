package com.gastromind.app.ui.cocina

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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.session.Sesion
import com.gastromind.app.ui.components.BarraSuperior
import com.gastromind.app.ui.components.EstadoVacio
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.pedidos.Comanda
import com.gastromind.app.ui.pedidos.PedidosEnVivoViewModel
import com.gastromind.app.ui.pedidos.PedidosUiState
import com.gastromind.app.ui.pedidos.PlatilloComanda
import com.gastromind.app.ui.pedidos.RefrescarMientrasSeVe
import com.gastromind.app.ui.pedidos.TextoPlatillo
import com.gastromind.app.ui.pedidos.Urgencia
import com.gastromind.app.ui.pedidos.hora
import com.gastromind.app.ui.pedidos.pasoDeCocina
import com.gastromind.app.ui.rol.InicioRolViewModel
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import com.gastromind.app.ui.theme.GastroMindCocinaTheme
import java.time.LocalDateTime

@Composable
fun ComandaRoute(
    sesion: Sesion,
    vm: PedidosEnVivoViewModel = viewModel(factory = PedidosEnVivoViewModel.factory(pasoDeCocina)),
    inicio: InicioRolViewModel = viewModel(factory = InicioRolViewModel.Factory),
    anularVm: AnularViewModel = viewModel(factory = AnularViewModel.Factory)
) {
    RefrescarMientrasSeVe(vm)
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val anular by anularVm.uiState.collectAsStateWithLifecycle()
    ComandaScreen(
        nombre = sesion.nombreCompleto,
        estado = estado,
        onAvanzar = vm::avanzar,
        onCerrarSesion = inicio::cerrarSesion,
        onAnular = anularVm::abrir
    )
    DialogoAnular(
        estado = anular,
        onMotivo = anularVm::onMotivo,
        onConfirmar = { anularVm.confirmar(vm::reemplazar) },
        onCancelar = anularVm::cerrar
    )
}

/** KDS del prototipo: oscuro y con tarjetas por mesa, para leerse de lejos en la tablet de cocina. */
@Composable
fun ComandaScreen(
    nombre: String,
    estado: PedidosUiState,
    onAvanzar: (Long, PlatilloComanda) -> Unit,
    onCerrarSesion: () -> Unit,
    onAnular: (Long, Int, PlatilloComanda) -> Unit = { _, _, _ -> }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Cocina",
                subtitulo = "$nombre · ${conteo(estado.comandas.size, "comanda")} · ${conteo(estado.totalPlatillos, "platillo")}",
                onCerrarSesion = onCerrarSesion,
                acciones = {
                    estado.ahora?.let {
                        Text(it.hora(), style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FuenteCifras), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            estado.error?.let { MensajeError(it, Modifier.padding(horizontal = Dimens.lg, vertical = Dimens.sm)) }
            when {
                estado.cargando -> PantallaCarga()
                estado.comandas.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EstadoVacio(
                        icono = Icons.Outlined.SoupKitchen,
                        titulo = "Sin comandas por ahora",
                        texto = "Los pedidos del salón aparecen aquí solos, con su hora de ingreso."
                    )
                }
                else -> LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(Dimens.anchoComanda),
                    contentPadding = PaddingValues(Dimens.lg),
                    verticalItemSpacing = Dimens.md,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.comandas, key = { it.pedidoId }) { comanda ->
                        TarjetaComanda(comanda, estado.cambiando, onAvanzar, onAnular)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaComanda(
    comanda: Comanda,
    cambiando: Set<Long>,
    onAvanzar: (Long, PlatilloComanda) -> Unit,
    onAnular: (Long, Int, PlatilloComanda) -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto) = when (comanda.urgencia) {
        Urgencia.DEMORADA -> colores.tertiaryContainer to colores.onTertiaryContainer
        Urgencia.NUEVA -> colores.primaryContainer to colores.onPrimaryContainer
        Urgencia.NORMAL -> colores.surfaceVariant to colores.onSurface
    }
    Surface(
        color = colores.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        border = if (comanda.urgencia == Urgencia.NUEVA) BorderStroke(Dimens.bordeDestacado, colores.secondary) else null
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(fondo).padding(horizontal = Dimens.md, vertical = Dimens.md)
            ) {
                val nueva = if (comanda.urgencia == Urgencia.NUEVA) " · nueva" else ""
                Text("M${comanda.mesaNumero}$nueva", style = MaterialTheme.typography.titleLarge, color = texto)
                Text(
                    "${comanda.horaIngreso.hora()} · ${comanda.minutos} min",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FuenteCifras),
                    color = texto
                )
            }
            comanda.platillos.forEach { platillo ->
                FilaPlatillo(
                    platillo = platillo,
                    ocupado = platillo.id in cambiando,
                    onAvanzar = { onAvanzar(comanda.pedidoId, platillo) },
                    onAnular = { onAnular(comanda.pedidoId, comanda.mesaNumero, platillo) }
                )
            }
        }
    }
}

@Composable
private fun FilaPlatillo(platillo: PlatilloComanda, ocupado: Boolean, onAvanzar: () -> Unit, onAnular: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.md, vertical = Dimens.sm)
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
            TextoPlatillo(platillo, Modifier.weight(1f))
        }
        BotonEstado(platillo, ocupado, onAvanzar)
        // HU-18: lo que ya se empezó se puede anular; sus insumos quedan como merma
        if (sePuedeAnularEnCocina(platillo.estado)) {
            IconButton(onClick = onAnular, enabled = !ocupado) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Anular ${platillo.nombre}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Dice en qué estado está el platillo; tocarlo lo pasa al siguiente. "Listo" ya no se toca. */
@Composable
private fun BotonEstado(platillo: PlatilloComanda, ocupado: Boolean, onAvanzar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto, etiqueta) = when (platillo.estado) {
        EstadoPlatillo.LISTO -> Triple(colores.primary, colores.onPrimary, "Listo")
        EstadoPlatillo.EN_PREPARACION -> Triple(colores.tertiary, colores.onTertiary, "Preparando")
        else -> Triple(colores.surfaceVariant, colores.onSurface, "Pendiente")
    }
    Button(
        onClick = onAvanzar,
        enabled = platillo.siguiente != null && !ocupado,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = Dimens.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = fondo,
            contentColor = texto,
            disabledContainerColor = fondo,
            disabledContentColor = texto
        ),
        modifier = Modifier.heightIn(min = Dimens.altoBotonEstado)
    ) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(widthDp = 1180, heightDp = 820)
@Composable
private fun ComandaPreview() {
    val ahora = LocalDateTime.of(2026, 11, 1, 13, 18)
    fun plato(id: Long, nombre: String, cantidad: Int, estado: EstadoPlatillo) =
        PlatilloComanda(id, nombre, cantidad, estado, pasoDeCocina(estado))
    GastroMindCocinaTheme {
        ComandaScreen(
            nombre = "Cocinero Sede Central",
            estado = PedidosUiState(
                cargando = false,
                ahora = ahora,
                comandas = listOf(
                    Comanda(1, 4, ahora.minusMinutes(13), 13, Urgencia.DEMORADA, listOf(
                        plato(1, "Lomo saltado", 2, EstadoPlatillo.LISTO),
                        plato(2, "Ceviche clásico", 1, EstadoPlatillo.EN_PREPARACION)
                    )),
                    Comanda(2, 2, ahora.minusMinutes(9), 9, Urgencia.NORMAL, listOf(
                        plato(3, "Ají de gallina", 1, EstadoPlatillo.EN_PREPARACION),
                        plato(4, "Arroz con pollo", 1, EstadoPlatillo.PENDIENTE)
                    )),
                    Comanda(3, 5, ahora.minusMinutes(1), 1, Urgencia.NUEVA, listOf(
                        plato(5, "Lomo saltado", 3, EstadoPlatillo.PENDIENTE)
                    ))
                )
            ),
            onAvanzar = { _, _ -> },
            onCerrarSesion = {}
        )
    }
}

private fun conteo(n: Int, nombre: String) = if (n == 1) "1 $nombre" else "$n ${nombre}s"
