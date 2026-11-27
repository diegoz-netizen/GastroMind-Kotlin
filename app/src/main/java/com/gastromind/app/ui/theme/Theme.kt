package com.gastromind.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
 * Claro en toda la app; la cocina usa sus roles de urgencia con GastroMindCocinaTheme.
 */
@Composable
fun GastroMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, typography = AppTypography, shapes = AppShapes, content = content)
}

// Mismos roles que antes, ahora en claro. Por urgencia y estado: primary = listo, tertiary = preparando,
// tertiaryContainer = demorada, primaryContainer + secondary = nueva, surfaceVariant = normal o pendiente
private val EsquemaCocina = lightColorScheme(
    primary = CocinaListo,
    onPrimary = SobrePrimario,
    primaryContainer = CocinaNueva,
    onPrimaryContainer = CocinaSobreNueva,
    secondary = CocinaBordeNueva,
    onSecondary = SobrePrimario,
    tertiary = CocinaPreparando,
    onTertiary = SobrePrimario,
    tertiaryContainer = CocinaContenedorPreparando,
    onTertiaryContainer = CocinaSobrePreparando,
    background = Fondo,
    onBackground = SobreSuperficie,
    surface = Fondo,
    onSurface = SobreSuperficie,
    surfaceVariant = VarianteSuperficie,
    onSurfaceVariant = SobreVarianteSuperficie,
    surfaceContainer = Superficie,
    surfaceContainerLowest = Superficie,
    surfaceContainerLow = Superficie,
    outline = Contorno,
    outlineVariant = ContornoVariante,
    error = Error,
    onError = SobreError,
    errorContainer = ContenedorError,
    onErrorContainer = SobreContenedorError
)

/**
 * Tema de la pantalla de cocina (KDS). Desde el rediseño uniforme es claro como el resto de la app,
 * con sus propios roles de urgencia (listo, preparando, demorada y nueva).
 */
@Composable
fun GastroMindCocinaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaCocina, typography = AppTypography, shapes = AppShapes, content = content)
}
