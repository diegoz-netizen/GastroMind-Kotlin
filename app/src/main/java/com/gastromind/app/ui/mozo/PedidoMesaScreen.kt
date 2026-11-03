package com.gastromind.app.ui.mozo

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.repository.Pedido
import com.gastromind.app.data.repository.Platillo
import com.gastromind.app.data.repository.PlatilloCarta
import com.gastromind.app.ui.components.BotonPrincipal
import com.gastromind.app.ui.components.BotonTexto
import com.gastromind.app.ui.components.EtiquetaEstado
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import com.gastromind.app.ui.theme.GastroMindTheme
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Composable
fun PedidoMesaRoute(
    mesaId: Long,
    mesaNumero: Int,
    onVolver: () -> Unit,
    vm: PedidoMesaViewModel = viewModel(factory = PedidoMesaViewModel.factory(mesaId))
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    // Enviado a cocina: se vuelve al salón, donde la mesa ya aparece ocupada
    LaunchedEffect(estado.enviado) {
        if (estado.enviado) onVolver()
    }
    PedidoMesaScreen(
        mesaNumero = mesaNumero,
        estado = estado,
        onSumar = vm::sumar,
        onRestar = vm::restar,
        onEnviar = vm::enviar,
        onReintentar = vm::cargar,
        onVolver = onVolver
    )
}

/** HU-09 (CA-09): la carta de la mesa. Con un pedido abierto, lo nuevo va como segunda ronda. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoMesaScreen(
    mesaNumero: Int,
    estado: PedidoMesaUiState,
    onSumar: (Long) -> Unit,
    onRestar: (Long) -> Unit,
    onEnviar: () -> Unit,
    onReintentar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mesa M$mesaNumero", style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (estado.segundaRonda) "Segunda ronda" else "Pedido nuevo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver al salón")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (!estado.cargando && estado.errorCarga == null) BarraEnviar(estado, onEnviar)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when {
                estado.cargando -> PantallaCarga()
                estado.errorCarga != null -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Dimens.lg)
                ) {
                    MensajeError(estado.errorCarga)
                    BotonTexto("Reintentar", onReintentar)
                }
                else -> Carta(estado, onSumar, onRestar)
            }
        }
    }
}

@Composable
private fun Carta(estado: PedidoMesaUiState, onSumar: (Long) -> Unit, onRestar: (Long) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(Dimens.lg),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        modifier = Modifier.widthIn(max = Dimens.anchoContenido).fillMaxSize()
    ) {
        estado.pedidoActual?.let { pedido ->
            item(key = "en-mesa") { YaEnLaMesa(pedido) }
        }
        if (estado.carta.isEmpty()) {
            item(key = "sin-carta") {
                Text(
                    "No hay platillos activos en la carta.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        estado.cartaPorCategoria.forEach { (categoria, platillos) ->
            item(key = "cat-$categoria") {
                Text(
                    categoria,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Dimens.md)
                )
            }
            items(platillos, key = { it.id }) { platillo ->
                FilaCarta(platillo, estado.cantidades[platillo.id] ?: 0, { onSumar(platillo.id) }, { onRestar(platillo.id) })
            }
        }
    }
}

@Composable
private fun YaEnLaMesa(pedido: Pedido) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(Dimens.borde, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs), modifier = Modifier.fillMaxWidth().padding(Dimens.md)) {
            Text("Ya pedido en la mesa", style = MaterialTheme.typography.titleMedium)
            pedido.platillos.filter { it.estado != EstadoPlatillo.ANULADO }.forEach { platillo ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${platillo.cantidad}× ${platillo.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    EtiquetaEstado(
                        textoEstado(platillo.estado),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun textoEstado(estado: EstadoPlatillo) = when (estado) {
    EstadoPlatillo.PENDIENTE -> "Pendiente"
    EstadoPlatillo.EN_PREPARACION -> "En preparación"
    EstadoPlatillo.LISTO -> "Listo"
    EstadoPlatillo.SERVIDO -> "Servido"
    EstadoPlatillo.ANULADO -> "Anulado"
    EstadoPlatillo.DESCONOCIDO -> "—"
}

@Composable
private fun FilaCarta(platillo: PlatilloCarta, cantidad: Int, onSumar: () -> Unit, onRestar: () -> Unit) {
    val elegido = cantidad > 0
    Surface(
        color = if (elegido) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(Dimens.borde, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.altoBotonEstado).padding(horizontal = Dimens.md, vertical = Dimens.xs)
        ) {
            Column(Modifier.weight(1f)) {
                Text(platillo.nombre, style = MaterialTheme.typography.bodyLarge)
                Text(
                    soles(platillo.precio),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FuenteCifras,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedIconButton(onClick = onRestar, enabled = elegido) {
                Icon(Icons.Outlined.Remove, contentDescription = "Quitar uno de ${platillo.nombre}")
            }
            Text(
                "$cantidad",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FuenteCifras,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = Dimens.xl)
            )
            FilledTonalIconButton(onClick = onSumar, enabled = cantidad < PedidoMesaViewModel.MAXIMO_POR_PLATILLO) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar uno de ${platillo.nombre}")
            }
        }
    }
}

@Composable
private fun BarraEnviar(estado: PedidoMesaUiState, onEnviar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = Dimens.xs) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(Dimens.lg)
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            estado.errorEnvio?.let { MensajeError(it, Modifier.widthIn(max = Dimens.anchoContenido)) }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.widthIn(max = Dimens.anchoContenido).fillMaxWidth()
            ) {
                Text(
                    if (estado.totalPlatillos == 1) "1 platillo" else "${estado.totalPlatillos} platillos",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(soles(estado.total), style = MaterialTheme.typography.titleMedium, fontFamily = FuenteCifras)
            }
            BotonPrincipal(
                texto = if (estado.segundaRonda) "Agregar al pedido" else "Enviar a cocina",
                onClick = onEnviar,
                habilitado = estado.puedeEnviar,
                cargando = estado.enviando,
                modifier = Modifier.widthIn(max = Dimens.anchoContenido)
            )
        }
    }
}

private fun soles(monto: BigDecimal) = "S/ " + monto.setScale(2, RoundingMode.HALF_UP).toPlainString()

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun PedidoMesaPreview() {
    val carta = listOf(
        PlatilloCarta(1, "Lomo saltado", BigDecimal("32.00"), "Fondos"),
        PlatilloCarta(2, "Arroz con pollo", BigDecimal("25.00"), "Fondos"),
        PlatilloCarta(3, "Chicha morada", BigDecimal("8.00"), "Bebidas")
    )
    val pedido = Pedido(7, 4, LocalDateTime.of(2026, 11, 3, 13, 0), listOf(
        Platillo(10, "Ceviche clásico", 1, EstadoPlatillo.SERVIDO)
    ))
    GastroMindTheme {
        PedidoMesaScreen(
            mesaNumero = 4,
            estado = PedidoMesaUiState(cargando = false, carta = carta, pedidoActual = pedido, cantidades = mapOf(1L to 2, 3L to 1)),
            onSumar = {}, onRestar = {}, onEnviar = {}, onReintentar = {}, onVolver = {}
        )
    }
}
