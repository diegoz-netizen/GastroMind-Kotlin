package com.gastromind.app.ui.mozo

import androidx.compose.material.icons.outlined.SoupKitchen
import com.gastromind.app.ui.components.TituloSeccion
import com.gastromind.app.ui.components.Seccion
import com.gastromind.app.ui.components.BarraInferior
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gastromind.app.data.repository.Mesa
import com.gastromind.app.data.session.Sesion
import com.gastromind.app.ui.components.BarraSuperior
import com.gastromind.app.ui.components.ChipEstado
import com.gastromind.app.ui.components.EstadoVacio
import com.gastromind.app.ui.components.Tono
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.pedidos.PedidosEnVivoViewModel
import com.gastromind.app.ui.pedidos.PedidosUiState
import com.gastromind.app.ui.pedidos.PlatilloComanda
import com.gastromind.app.ui.pedidos.RefrescarMientrasSeVe
import com.gastromind.app.ui.pedidos.pasoDeMozo
import com.gastromind.app.ui.rol.InicioRolViewModel
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import com.gastromind.app.ui.theme.GastroMindTheme

private val SECCIONES = listOf(Seccion("Salón", Icons.Outlined.TableRestaurant), Seccion("En cocina", Icons.Outlined.SoupKitchen))

@Composable
fun SalonRoute(
    sesion: Sesion,
    onAbrirMesa: (Mesa) -> Unit,
    mesasVm: MesasViewModel = viewModel(factory = MesasViewModel.Factory),
    cocinaVm: PedidosEnVivoViewModel = viewModel(factory = PedidosEnVivoViewModel.factory(pasoDeMozo)),
    inicio: InicioRolViewModel = viewModel(factory = InicioRolViewModel.Factory)
) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val mesas by mesasVm.uiState.collectAsStateWithLifecycle()
    val cocina by cocinaVm.uiState.collectAsStateWithLifecycle()

    // Al volver de tomar un pedido, la mesa ya aparece ocupada
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(mesasVm, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { mesasVm.cargar() }
    }
    if (pestana == 1) RefrescarMientrasSeVe(cocinaVm)

    SalonScreen(
        nombre = sesion.nombreCompleto,
        pestana = pestana,
        onPestana = { pestana = it },
        mesas = mesas,
        onReintentarMesas = mesasVm::cargar,
        onAbrirMesa = onAbrirMesa,
        cocina = cocina,
        onServir = cocinaVm::avanzar,
        onCerrarSesion = inicio::cerrarSesion
    )
}

/** Inicio del mozo: las mesas del salón (HU-09) y lo que está en cocina (HU-11). */
@Composable
fun SalonScreen(
    nombre: String,
    pestana: Int,
    onPestana: (Int) -> Unit,
    mesas: MesasUiState,
    onReintentarMesas: () -> Unit,
    onAbrirMesa: (Mesa) -> Unit,
    cocina: PedidosUiState,
    onServir: (Long, PlatilloComanda) -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            BarraSuperior(titulo = if (pestana == 0) "Salón" else "En cocina", subtitulo = nombre, onCerrarSesion = onCerrarSesion)
        },
        // Diseño móvil v3: las secciones del mozo van en la barra inferior
        bottomBar = { BarraInferior(SECCIONES, pestana, onPestana) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
            when (pestana) {
                0 -> MesasContenido(mesas, onReintentarMesas, onAbrirMesa)
                else -> EnCocinaContenido(cocina, onServir)
            }
        }
    }
}

@Composable
private fun MesasContenido(estado: MesasUiState, onReintentar: () -> Unit, onAbrirMesa: (Mesa) -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        estado.error?.let {
            MensajeError(it, Modifier.padding(horizontal = Dimens.lg, vertical = Dimens.sm).clickable(onClick = onReintentar))
        }
        when {
            estado.cargando -> PantallaCarga()
            estado.mesas.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EstadoVacio(
                    icono = Icons.Outlined.TableRestaurant,
                    titulo = "Sin mesas",
                    texto = "El restaurante todavía no tiene mesas registradas.",
                    etiqueta = "Gestión de mesas · HU-26"
                )
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(Dimens.anchoMesa),
                contentPadding = PaddingValues(Dimens.lg),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
                modifier = Modifier.widthIn(max = Dimens.anchoContenido).fillMaxSize()
            ) {
                // Tira de indicadores del diseño móvil v3: cuántas mesas hay y cómo están
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val libres = estado.mesas.count { it.libre }
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                        Indicador("Mesas", estado.mesas.size, Modifier.weight(1f))
                        Indicador("Libres", libres, Modifier.weight(1f))
                        Indicador("Ocupadas", estado.mesas.size - libres, Modifier.weight(1f), destacado = true)
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    TituloSeccion("Mapa del salón", "Mesas", Modifier.padding(top = Dimens.sm))
                }
                items(estado.mesas, key = { it.id }) { mesa -> TarjetaMesaSalon(mesa) { onAbrirMesa(mesa) } }
            }
        }
    }
}

@Composable
private fun Indicador(etiqueta: String, valor: Int, modifier: Modifier = Modifier, destacado: Boolean = false) {
    val colores = MaterialTheme.colorScheme
    Surface(
        color = colores.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(Dimens.borde, colores.outlineVariant),
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm)) {
            Text(etiqueta.uppercase(), style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant)
            Text(
                "$valor",
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FuenteCifras),
                fontWeight = FontWeight.Bold,
                color = if (destacado) colores.tertiary else colores.onSurface
            )
        }
    }
}

@Composable
private fun TarjetaMesaSalon(mesa: Mesa, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    // Rediseño uniforme: tarjeta blanca para todas; el estado lo dicen el borde y el chip (como en la web)
    Surface(
        onClick = onClick,
        color = colores.surfaceContainerLowest,
        contentColor = colores.onSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(if (mesa.libre) Dimens.borde else Dimens.bordeDestacado, if (mesa.libre) colores.outlineVariant else colores.tertiary),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(Dimens.md)
        ) {
            Text(
                "M${mesa.numero}",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FuenteCifras,
                fontWeight = FontWeight.Bold
            )
            ChipEstado(if (mesa.libre) "Libre" else "Ocupada", if (mesa.libre) Tono.Ok else Tono.Aviso)
            // Pie del v3: qué pasa al tocarla
            Text(
                if (mesa.libre) "Abrir →" else "Ver pedido →",
                style = MaterialTheme.typography.labelLarge,
                color = if (mesa.libre) colores.primary else colores.onTertiaryContainer
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun SalonPreview() {
    GastroMindTheme {
        SalonScreen(
            nombre = "Mozo Sede Central",
            pestana = 0,
            onPestana = {},
            mesas = MesasUiState(
                cargando = false,
                mesas = (1..8).map { Mesa(it.toLong(), it, libre = it % 3 != 0) }
            ),
            onReintentarMesas = {},
            onAbrirMesa = {},
            cocina = PedidosUiState(cargando = false),
            onServir = { _, _ -> },
            onCerrarSesion = {}
        )
    }
}
