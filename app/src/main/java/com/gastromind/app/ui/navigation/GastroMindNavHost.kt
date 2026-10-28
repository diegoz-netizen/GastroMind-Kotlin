package com.gastromind.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gastromind.app.data.session.EstadoSesion
import com.gastromind.app.data.session.Rol
import com.gastromind.app.data.session.SesionStore
import com.gastromind.app.ui.components.PantallaCarga
import com.gastromind.app.ui.login.LoginRoute
import com.gastromind.app.ui.rol.InicioRolRoute

/** Rutas de la app. Cada HU agrega las suyas dentro de su rol (p. ej. "mozo/pedido/{mesaId}"). */
object Rutas {
    const val LOGIN = "login"
    const val MOZO = "mozo"
    const val COCINA = "cocina"
    const val ALMACEN = "almacen"
    const val SIN_APP = "sin-app"

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
        listOf(Rutas.MOZO, Rutas.COCINA, Rutas.ALMACEN, Rutas.SIN_APP).forEach { ruta ->
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
