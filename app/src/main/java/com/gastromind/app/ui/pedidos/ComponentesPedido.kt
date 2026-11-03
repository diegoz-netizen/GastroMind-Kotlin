package com.gastromind.app.ui.pedidos

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.gastromind.app.ui.theme.FuenteCifras
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")

fun LocalDateTime.hora(): String = format(FORMATO_HORA)

/** Refresca los pedidos solo con la pantalla a la vista: en segundo plano no gasta red ni batería. */
@Composable
fun RefrescarMientrasSeVe(vm: PedidosEnVivoViewModel) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(vm, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { vm.mantenerActualizado() }
    }
}

/** "2× Lomo saltado", con la cantidad en cifras como en el prototipo. */
@Composable
fun TextoPlatillo(platillo: PlatilloComanda, modifier: Modifier = Modifier) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontFamily = FuenteCifras, fontWeight = FontWeight.Bold)) { append("${platillo.cantidad}×") }
            append(" ${platillo.nombre}")
        },
        style = LocalTextStyle.current,
        modifier = modifier
    )
}
