package com.gastromind.app.ui.mozo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gastromind.app.data.remote.dto.PlatilloCarta
import com.gastromind.app.data.session.Sesion
import com.gastromind.app.ui.components.BarraSuperior
import com.gastromind.app.ui.components.BotonPrincipal
import com.gastromind.app.ui.components.BotonTexto
import com.gastromind.app.ui.components.CampoSeleccion
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.rol.InicioRolViewModel
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras

@Composable
fun MozoRoute(
    sesion: Sesion,
    inicioVm: InicioRolViewModel = viewModel(factory = InicioRolViewModel.Factory),
    vm: MozoViewModel = viewModel(factory = MozoViewModel.Factory)
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    MozoScreen(
        sesion = sesion,
        estado = estado,
        onCerrarSesion = inicioVm::cerrarSesion,
        onReintentar = vm::cargar,
        onMesa = vm::onMesa,
        onMas = vm::onMas,
        onMenos = vm::onMenos,
        onEnviar = vm::enviar
    )
}

@Composable
fun MozoScreen(
    sesion: Sesion,
    estado: MozoUiState,
    onCerrarSesion: () -> Unit,
    onReintentar: () -> Unit,
    onMesa: (Long) -> Unit,
    onMas: (Long) -> Unit,
    onMenos: (Long) -> Unit,
    onEnviar: () -> Unit
) {
    val listo = !estado.cargando && estado.errorCarga == null
    Scaffold(
        topBar = { BarraSuperior(titulo = "Salón", subtitulo = sesion.nombreCompleto, onCerrarSesion = onCerrarSesion) },
        bottomBar = {
            if (listo) {
                Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.navigationBarsPadding()) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.lg, vertical = Dimens.md)
                        ) {
                            Text(
                                "${estado.totalPlatillos} platillo(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                precioConFormato(estado.total),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FuenteCifras
                            )
                        }
                        BotonPrincipal(
                            texto = "Enviar pedido",
                            onClick = onEnviar,
                            cargando = estado.enviando,
                            modifier = Modifier.padding(start = Dimens.lg, end = Dimens.lg, bottom = Dimens.lg)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when {
                estado.cargando -> PantallaCarga()
                estado.errorCarga != null -> Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = Dimens.anchoFormulario).padding(Dimens.lg)
                ) {
                    MensajeError(estado.errorCarga)
                    BotonTexto("Reintentar", onReintentar)
                }
                else -> NuevoPedido(estado, onMesa, onMas, onMenos)
            }
        }
    }
}

@Composable
private fun NuevoPedido(
    estado: MozoUiState,
    onMesa: (Long) -> Unit,
    onMas: (Long) -> Unit,
    onMenos: (Long) -> Unit
) {
    val porCategoria = remember(estado.carta) { estado.carta.groupBy { it.categoriaNombre ?: "Otros" } }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        contentPadding = PaddingValues(Dimens.lg),
        modifier = Modifier.widthIn(max = Dimens.anchoContenido).fillMaxWidth()
    ) {
        item { Text("Nuevo pedido", style = MaterialTheme.typography.titleLarge) }
        item {
            CampoSeleccion(
                etiqueta = "Mesa",
                opciones = estado.mesasLibres,
                seleccion = estado.mesa,
                texto = { "Mesa ${it.numero}" },
                onSeleccion = { onMesa(it.id) }
            )
        }
        if (estado.mesasLibres.isEmpty()) {
            item {
                Text(
                    "No hay mesas libres por ahora.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        estado.error?.let { item { MensajeError(it) } }
        estado.exito?.let { item { MensajeExito(it) } }
        if (estado.carta.isEmpty()) {
            item {
                Text(
                    "La carta no tiene platillos activos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        porCategoria.forEach { (categoria, platillos) ->
            item {
                Text(
                    categoria,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Dimens.sm)
                )
            }
            items(platillos, key = { it.id }) { platillo ->
                FilaPlatillo(
                    platillo = platillo,
                    cantidad = estado.cantidadDe(platillo.id),
                    onMas = { onMas(platillo.id) },
                    onMenos = { onMenos(platillo.id) }
                )
            }
        }
    }
}

@Composable
private fun FilaPlatillo(platillo: PlatilloCarta, cantidad: Int, onMas: () -> Unit, onMenos: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Dimens.lg, vertical = Dimens.sm)
        ) {
            Column(Modifier.weight(1f)) {
                Text(platillo.nombre, style = MaterialTheme.typography.bodyLarge)
                Text(
                    precioConFormato(platillo.precio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FuenteCifras
                )
            }
            IconButton(onClick = onMenos, enabled = cantidad > 0) {
                Icon(Icons.Outlined.Remove, contentDescription = "Quitar ${platillo.nombre}")
            }
            Text(
                cantidad.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FuenteCifras,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = Dimens.xl)
            )
            IconButton(onClick = onMas) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar ${platillo.nombre}")
            }
        }
    }
}

@Composable
private fun MensajeExito(texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Dimens.md))
    }
}
