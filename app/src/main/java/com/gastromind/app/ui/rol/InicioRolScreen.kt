package com.gastromind.app.ui.rol

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RoomService
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gastromind.app.data.session.Rol
import com.gastromind.app.data.session.Sesion
import com.gastromind.app.ui.components.BarraSuperior
import com.gastromind.app.ui.components.EstadoVacio
import com.gastromind.app.ui.theme.GastroMindTheme

/** Qué muestra el inicio de cada rol. Cada dueño reemplaza su pantalla al hacer su HU. */
private data class InfoRol(val seccion: String, val icono: ImageVector, val titulo: String, val texto: String, val etiqueta: String?)

private fun infoDe(rol: Rol) = when (rol) {
    Rol.MOZO -> InfoRol("Salón", Icons.Outlined.RoomService, "Toma de pedidos",
        "Aquí el mozo elige la mesa, toma el pedido y cierra la cuenta.", "HU-09 y HU-12 · Piero")
    Rol.COCINERO -> InfoRol("Cocina", Icons.Outlined.SoupKitchen, "Comandas de cocina",
        "Aquí llegan los pedidos del salón y el cocinero cambia el estado de cada platillo.", "HU-10 y HU-11 · Diego")
    Rol.ALMACENERO -> InfoRol("Almacén", Icons.Outlined.Inventory2, "Ingreso de compras",
        "Aquí el almacenero registra lo que llega de los proveedores y consulta el kardex.", "HU-14 y HU-15 · Victor")
    Rol.ADMINISTRADOR, Rol.DESCONOCIDO -> InfoRol("GastroMind", Icons.Outlined.Computer, "Usa la web",
        "La app es para el personal del salón, la cocina y el almacén. La administración se hace desde la web.", null)
}

@Composable
fun InicioRolRoute(sesion: Sesion, vm: InicioRolViewModel = viewModel(factory = InicioRolViewModel.Factory)) {
    InicioRolScreen(sesion = sesion, onCerrarSesion = vm::cerrarSesion)
}

@Composable
fun InicioRolScreen(sesion: Sesion, onCerrarSesion: () -> Unit) {
    val info = infoDe(sesion.rol)
    Scaffold(
        topBar = { BarraSuperior(titulo = info.seccion, subtitulo = sesion.nombreCompleto, onCerrarSesion = onCerrarSesion) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            EstadoVacio(icono = info.icono, titulo = info.titulo, texto = info.texto, etiqueta = info.etiqueta?.let { "En construcción · $it" })
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun InicioCocinaPreview() {
    GastroMindTheme {
        InicioRolScreen(Sesion("t", 1, "Cocinero Sede Central", "cocina.central@gastromind.demo", Rol.COCINERO, 1), onCerrarSesion = {})
    }
}
