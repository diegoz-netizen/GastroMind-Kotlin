package com.gastromind.app.ui.almacen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gastromind.app.data.remote.dto.InsumoOpcion
import com.gastromind.app.data.remote.dto.ProveedorOpcion
import com.gastromind.app.data.session.Rol
import com.gastromind.app.data.session.Sesion
import com.gastromind.app.ui.components.BarraSuperior
import com.gastromind.app.ui.components.BotonPrincipal
import com.gastromind.app.ui.components.CampoFecha
import com.gastromind.app.ui.components.CampoSeleccion
import com.gastromind.app.ui.components.CampoTexto
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.components.Pestanas
import com.gastromind.app.ui.rol.InicioRolViewModel
import com.gastromind.app.ui.theme.Dimens
import com.gastromind.app.ui.theme.FuenteCifras
import com.gastromind.app.ui.theme.GastroMindTheme
import java.math.BigDecimal
import java.time.LocalDate

private const val PESTANA_INGRESO = 0
private const val PESTANA_KARDEX = 1
private val PESTANAS = listOf("Ingreso", "Kardex")

@Composable
fun AlmacenRoute(
    sesion: Sesion,
    inicioVm: InicioRolViewModel = viewModel(factory = InicioRolViewModel.Factory),
    ingresoVm: IngresoCompraViewModel = viewModel(factory = IngresoCompraViewModel.Factory),
    kardexVm: KardexViewModel = viewModel(factory = KardexViewModel.Factory)
) {
    val estado by ingresoVm.uiState.collectAsStateWithLifecycle()
    val kardex by kardexVm.uiState.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableIntStateOf(PESTANA_INGRESO) }

    // Cada vez que se abre: una compra recién registrada ya tiene que verse en el kardex
    LaunchedEffect(pestana) {
        if (pestana == PESTANA_KARDEX) kardexVm.actualizar()
    }

    AlmacenScreen(
        sesion = sesion,
        pestana = pestana,
        onPestana = { pestana = it },
        onCerrarSesion = inicioVm::cerrarSesion,
        estado = estado,
        onReintentar = ingresoVm::cargarOpciones,
        onProveedor = ingresoVm::onProveedor,
        onInsumo = ingresoVm::onInsumo,
        onCantidad = ingresoVm::onCantidad,
        onCostoTotal = ingresoVm::onCostoTotal,
        onLote = ingresoVm::onLote,
        onVencimiento = ingresoVm::onVencimiento,
        onRegistrar = ingresoVm::registrar,
        kardex = kardex,
        onReintentarKardex = kardexVm::actualizar,
        onInsumoKardex = kardexVm::onInsumo,
        onDesde = kardexVm::onDesde,
        onHasta = kardexVm::onHasta
    )
}

@Composable
fun AlmacenScreen(
    sesion: Sesion,
    pestana: Int,
    onPestana: (Int) -> Unit,
    onCerrarSesion: () -> Unit,
    estado: IngresoCompraUiState,
    onReintentar: () -> Unit,
    onProveedor: (Long) -> Unit,
    onInsumo: (Long) -> Unit,
    onCantidad: (String) -> Unit,
    onCostoTotal: (String) -> Unit,
    onLote: (String) -> Unit,
    onVencimiento: (LocalDate?) -> Unit,
    onRegistrar: () -> Unit,
    kardex: KardexUiState,
    onReintentarKardex: () -> Unit,
    onInsumoKardex: (Long) -> Unit,
    onDesde: (LocalDate?) -> Unit,
    onHasta: (LocalDate?) -> Unit
) {
    val formularioListo = pestana == PESTANA_INGRESO && !estado.cargando && estado.errorCarga == null
    Scaffold(
        topBar = { BarraSuperior(titulo = "Almacén", subtitulo = sesion.nombreCompleto, onCerrarSesion = onCerrarSesion) },
        bottomBar = {
            if (formularioListo) {
                Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.navigationBarsPadding().imePadding()) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        BotonPrincipal(
                            texto = "Registrar ingreso",
                            onClick = onRegistrar,
                            cargando = estado.guardando,
                            modifier = Modifier.padding(Dimens.lg)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Pestanas(PESTANAS, pestana, onPestana, Modifier.padding(horizontal = Dimens.lg, vertical = Dimens.sm))
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                if (pestana == PESTANA_INGRESO) {
                    IngresoCompra(
                        estado, onReintentar, onProveedor, onInsumo, onCantidad, onCostoTotal, onLote, onVencimiento
                    )
                } else {
                    KardexPestana(kardex, onReintentarKardex, onInsumoKardex, onDesde, onHasta)
                }
            }
        }
    }
}

@Composable
private fun IngresoCompra(
    estado: IngresoCompraUiState,
    onReintentar: () -> Unit,
    onProveedor: (Long) -> Unit,
    onInsumo: (Long) -> Unit,
    onCantidad: (String) -> Unit,
    onCostoTotal: (String) -> Unit,
    onLote: (String) -> Unit,
    onVencimiento: (LocalDate?) -> Unit
) {
    when {
        estado.cargando -> PantallaCarga()
        estado.errorCarga != null -> Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.lg),
            modifier = Modifier.widthIn(max = Dimens.anchoFormulario).padding(Dimens.lg)
        ) {
            MensajeError(estado.errorCarga)
            BotonPrincipal("Reintentar", onClick = onReintentar)
        }
        else -> Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
            modifier = Modifier
                .widthIn(max = Dimens.anchoFormulario)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.lg)
        ) {
            Text("Registrar ingreso", style = MaterialTheme.typography.titleLarge)
            CampoSeleccion(
                etiqueta = "Proveedor",
                opciones = estado.proveedores,
                seleccion = estado.proveedor,
                texto = { it.nombre },
                onSeleccion = { onProveedor(it.id) }
            )
            CampoSeleccion(
                etiqueta = "Insumo",
                opciones = estado.insumos,
                seleccion = estado.insumo,
                texto = { "${it.nombre} · quedan ${cantidadConFormato(it.stockActual)} ${it.unidadMedida}" },
                onSeleccion = { onInsumo(it.id) }
            )
            CampoTexto(
                valor = estado.cantidad,
                onCambio = onCantidad,
                etiqueta = estado.insumo?.let { "Cantidad (${it.unidadMedida})" } ?: "Cantidad",
                tipoTeclado = KeyboardType.Decimal
            )
            CampoTexto(
                valor = estado.costoTotal,
                onCambio = onCostoTotal,
                etiqueta = "Costo total (S/)",
                tipoTeclado = KeyboardType.Decimal
            )
            CampoTexto(
                valor = estado.lote,
                onCambio = onLote,
                etiqueta = "Lote (opcional)",
                accion = ImeAction.Done
            )
            CampoFecha(
                etiqueta = "Vencimiento (opcional)",
                fecha = estado.vencimiento,
                onFecha = onVencimiento,
                desde = LocalDate.now()
            )
            estado.stockDespues?.let { stock ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                        modifier = Modifier.padding(Dimens.lg)
                    ) {
                        Text(
                            "Stock después",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${cantidadConFormato(stock)} ${estado.insumo?.unidadMedida.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FuenteCifras
                        )
                    }
                }
            }
            estado.error?.let { MensajeError(it) }
            estado.exito?.let {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Dimens.md))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun AlmacenPreview() {
    GastroMindTheme {
        AlmacenScreen(
            sesion = Sesion("t", 1, "Almacenero Sede Central", "almacen.central@gastromind.demo", Rol.ALMACENERO, 1),
            pestana = PESTANA_INGRESO,
            onPestana = {},
            onCerrarSesion = {},
            estado = IngresoCompraUiState(
                cargando = false,
                proveedores = listOf(ProveedorOpcion(1, "Mercado Mayorista")),
                insumos = listOf(InsumoOpcion(1, "Arroz", "kg", BigDecimal("25.500"))),
                proveedorId = 1,
                insumoId = 1,
                cantidad = "10,5"
            ),
            onReintentar = {},
            onProveedor = {},
            onInsumo = {},
            onCantidad = {},
            onCostoTotal = {},
            onLote = {},
            onVencimiento = {},
            onRegistrar = {},
            kardex = KardexUiState(),
            onReintentarKardex = {},
            onInsumoKardex = {},
            onDesde = {},
            onHasta = {}
        )
    }
}
