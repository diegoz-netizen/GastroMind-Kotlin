package com.gastromind.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Esquema = lightColorScheme(
    primary = Primario,
    onPrimary = SobrePrimario,
    primaryContainer = ContenedorPrimario,
    onPrimaryContainer = SobreContenedorPrimario,
    secondary = Secundario,
    onSecondary = SobreSecundario,
    secondaryContainer = ContenedorSecundario,
    onSecondaryContainer = SobreContenedorSecundario,
    tertiary = Terciario,
    onTertiary = SobreTerciario,
    tertiaryContainer = ContenedorTerciario,
    onTertiaryContainer = SobreContenedorTerciario,
    background = Fondo,
    onBackground = SobreSuperficie,
    surface = Superficie,
    onSurface = SobreSuperficie,
    surfaceVariant = VarianteSuperficie,
    onSurfaceVariant = SobreVarianteSuperficie,
    surfaceContainerLowest = Superficie,
    surfaceContainerLow = Superficie,
    surfaceContainer = VarianteSuperficie,
    outline = Contorno,
    outlineVariant = ContornoVariante,
    error = Error,
    onError = SobreError,
    errorContainer = ContenedorError,
    onErrorContainer = SobreContenedorError
)

/**
 * Tema único de la app. Sin color dinámico: la identidad no cambia con el fondo del teléfono.
 * Claro en toda la app; la cocina usa su esquema oscuro con GastroMindCocinaTheme.
 */
@Composable
fun GastroMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, typography = AppTypography, shapes = AppShapes, content = content)
}

// Mismos roles que el tema claro. Por urgencia y estado: primary = listo, tertiary = preparando,
// tertiaryContainer = demorada, primaryContainer + secondary = nueva, surfaceVariant = normal o pendiente
private val EsquemaCocina = darkColorScheme(
    primary = CocinaListo,
    onPrimary = SobrePrimario,
    primaryContainer = CocinaNueva,
    onPrimaryContainer = CocinaTexto,
    secondary = CocinaBordeNueva,
    onSecondary = CocinaFondo,
    tertiary = CocinaPreparando,
    onTertiary = CocinaFondo,
    tertiaryContainer = CocinaDemorada,
    onTertiaryContainer = SobrePrimario,
    background = CocinaFondo,
    onBackground = CocinaTexto,
    surface = CocinaFondo,
    onSurface = CocinaTexto,
    surfaceVariant = CocinaNeutro,
    onSurfaceVariant = CocinaTextoSuave,
    surfaceContainer = CocinaPanel,
    outline = CocinaNeutro,
    outlineVariant = CocinaNeutro,
    error = ContenedorError,
    onError = SobreContenedorError,
    errorContainer = SobreContenedorError,
    onErrorContainer = ContenedorError
)

/** Tema de la pantalla de cocina (KDS). Con fondo oscuro, los íconos de la barra del sistema van claros. */
@Composable
fun GastroMindCocinaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val barras = WindowCompat.getInsetsController((view.context as Activity).window, view)
            barras.isAppearanceLightStatusBars = false
            onDispose { barras.isAppearanceLightStatusBars = true }
        }
    }
    MaterialTheme(colorScheme = EsquemaCocina, typography = AppTypography, shapes = AppShapes, content = content)
}
