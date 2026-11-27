package com.gastromind.app.ui.theme

import androidx.compose.ui.graphics.Color

// Sistema de diseño uniforme (27/11): el mismo verde, neutros fríos y colores de estado que la web
// de Spring y el panel de Django. Ámbar solo para avisos (ocupada, pendiente, bajo mínimo).
// Nombres por rol, no por color: las pantallas usan MaterialTheme.colorScheme.*
internal val Primario = Color(0xFF1D5B47)
internal val SobrePrimario = Color(0xFFFFFFFF)
internal val ContenedorPrimario = Color(0xFFDCEBE4)
internal val SobreContenedorPrimario = Color(0xFF0E3327)

internal val Secundario = Color(0xFF4E6359)
internal val SobreSecundario = Color(0xFFFFFFFF)
internal val ContenedorSecundario = Color(0xFFE3EAE6)
internal val SobreContenedorSecundario = Color(0xFF1F2B25)

internal val Terciario = Color(0xFFC98A1E)
internal val SobreTerciario = Color(0xFFFFFFFF)
internal val ContenedorTerciario = Color(0xFFFCF1DC)
internal val SobreContenedorTerciario = Color(0xFF6B4400)

internal val Fondo = Color(0xFFF4F6F5)   // gris frío: antes era crema
internal val Superficie = Color(0xFFFFFFFF)
internal val SobreSuperficie = Color(0xFF18201C)
internal val VarianteSuperficie = Color(0xFFEEF1EF)
internal val SobreVarianteSuperficie = Color(0xFF5B6661)
internal val Contorno = Color(0xFFC9D1CC)
internal val ContornoVariante = Color(0xFFDDE3DF)

internal val Error = Color(0xFFB42318)
internal val SobreError = Color(0xFFFFFFFF)
internal val ContenedorError = Color(0xFFFDECEA)
internal val SobreContenedorError = Color(0xFF5C0F09)

// Cocina (KDS) en claro, como el resto de la app: mismas superficies; la urgencia la dan los colores de estado
internal val CocinaListo = Color(0xFF2E7D4F)
internal val CocinaPreparando = Color(0xFFC98A1E)
internal val CocinaContenedorPreparando = Color(0xFFFCF1DC)
internal val CocinaSobrePreparando = Color(0xFF6B4400)
internal val CocinaNueva = Color(0xFFE3F4EA)
internal val CocinaSobreNueva = Color(0xFF0E3327)
internal val CocinaBordeNueva = Color(0xFF2E7D4F)
