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
 * Solo claro por ahora; el KDS oscuro del prototipo (HU-10) se suma como esquema aparte.
 */
@Composable
fun GastroMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, typography = AppTypography, shapes = AppShapes, content = content)
}
