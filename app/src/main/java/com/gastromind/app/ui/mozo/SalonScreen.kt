package com.gastromind.app.ui.mozo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
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
import com.gastromind.app.ui.components.EstadoVacio
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.components.Pestanas
import com.gastromind.app.ui.pedidos.PedidosEnVivoViewModel
import com.gastromind.app.ui.pedidos.PedidosUiState
import com.gastromind.app.ui.pedidos.PlatilloComanda
import com.gastromind.app.ui.pedidos.RefrescarMientrasSeVe
import com.gastromind.app.ui.pedidos.pasoDeMozo
import com.gastromind.app.ui.rol.InicioRolViewModel
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import com.gastromind.app.ui.theme.GastroMindTheme

private val PESTANAS = listOf("Mesas", "En cocina")

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
        topBar = { BarraSuperior(titulo = "Salón", subtitulo = nombre, onCerrarSesion = onCerrarSesion) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
            Pestanas(
                PESTANAS, pestana, onPestana,
                Modifier.widthIn(max = Dimens.anchoContenido).padding(horizontal = Dimens.lg, vertical = Dimens.sm)
            )
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
                items(estado.mesas, key = { it.id }) { mesa -> TarjetaMesaSalon(mesa) { onAbrirMesa(mesa) } }
            }
        }
    }
}

@Composable
private fun TarjetaMesaSalon(mesa: Mesa, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        color = if (mesa.libre) colores.surface else colores.tertiaryContainer,
        contentColor = if (mesa.libre) colores.onSurface else colores.onTertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(Dimens.borde, if (mesa.libre) colores.outlineVariant else colores.tertiary),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Dimens.sm)
        ) {
            Text(
                "M${mesa.numero}",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FuenteCifras,
                fontWeight = FontWeight.Bold
            )
            Text(if (mesa.libre) "Libre" else "Ocupada", style = MaterialTheme.typography.labelMedium)
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
