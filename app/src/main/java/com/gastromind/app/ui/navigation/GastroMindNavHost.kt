package com.gastromind.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gastromind.app.data.session.EstadoSesion
import com.gastromind.app.data.session.Rol
import com.gastromind.app.data.session.SesionStore
import com.gastromind.app.ui.almacen.AlmacenRoute
import com.gastromind.app.ui.cocina.ComandaRoute
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.login.LoginRoute
import com.gastromind.app.ui.mozo.PedidoMesaRoute
import com.gastromind.app.ui.mozo.SalonRoute
import com.gastromind.app.ui.rol.InicioRolRoute
import com.gastromind.app.ui.theme.GastroMindCocinaTheme

/** Rutas de la app. Cada HU agrega las suyas dentro de su rol (p. ej. "mozo/pedido/{mesaId}"). */
object Rutas {
    const val LOGIN = "login"
    const val MOZO = "mozo"
    const val COCINA = "cocina"
    const val ALMACEN = "almacen"
    const val SIN_APP = "sin-app"
    const val MOZO_MESA = "mozo/mesa/{mesaId}/{numero}"

    fun mesa(mesaId: Long, numero: Int) = "mozo/mesa/$mesaId/$numero"

    fun inicioDe(rol: Rol) = when (rol) {
        Rol.MOZO -> MOZO
        Rol.COCINERO -> COCINA
        Rol.ALMACENERO -> ALMACEN
        Rol.ADMINISTRADOR, Rol.DESCONOCIDO -> SIN_APP
    }
}

/**
 * La sesión manda: sin token se ve el login; con token, el inicio de su rol.
 * Así, entrar, cerrar sesión o un 401 (empleado desactivado) llevan solos a la pantalla correcta.
 */
@Composable
fun GastroMindNavHost(sesionStore: SesionStore) {
    val estado by sesionStore.estado.collectAsStateWithLifecycle()
    val actual = estado
    if (actual is EstadoSesion.Cargando) {
        PantallaCarga()
        return
    }

    val nav = rememberNavController()
    val destino = when (actual) {
        is EstadoSesion.Activa -> Rutas.inicioDe(actual.sesion.rol)
        else -> Rutas.LOGIN
    }

    NavHost(navController = nav, startDestination = destino) {
        composable(Rutas.LOGIN) { LoginRoute() }
        composable(Rutas.COCINA) {
            (estado as? EstadoSesion.Activa)?.let { GastroMindCocinaTheme { ComandaRoute(sesion = it.sesion) } }
        }
        composable(Rutas.MOZO) {
            (estado as? EstadoSesion.Activa)?.let {
                SalonRoute(sesion = it.sesion, onAbrirMesa = { mesa -> nav.navigate(Rutas.mesa(mesa.id, mesa.numero)) })
            }
        }
        composable(
            Rutas.MOZO_MESA,
            arguments = listOf(navArgument("mesaId") { type = NavType.LongType }, navArgument("numero") { type = NavType.IntType })
        ) { entrada ->
            val argumentos = entrada.arguments ?: return@composable
            PedidoMesaRoute(
                mesaId = argumentos.getLong("mesaId"),
                mesaNumero = argumentos.getInt("numero"),
                onVolver = { nav.popBackStack() }
            )
        }
        composable(Rutas.ALMACEN) {
            (estado as? EstadoSesion.Activa)?.let { AlmacenRoute(sesion = it.sesion) }
        }
        listOf(Rutas.SIN_APP).forEach { ruta ->
            composable(ruta) {
                (estado as? EstadoSesion.Activa)?.let { InicioRolRoute(sesion = it.sesion) }
            }
        }
    }

    LaunchedEffect(destino) {
        if (nav.currentDestination?.route != destino) {
            nav.navigate(destino) {
                // Sin volver atrás: después de cerrar sesión, "atrás" no regresa a la pantalla del rol
                popUpTo(nav.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
