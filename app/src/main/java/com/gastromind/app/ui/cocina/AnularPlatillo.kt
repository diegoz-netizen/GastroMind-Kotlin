package com.gastromind.app.ui.cocina

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gastromind.app.GastroMindApp
import com.gastromind.app.data.remote.dto.MotivoMermaDto
import com.gastromind.app.data.repository.AnularRepository
import com.gastromind.app.data.repository.EstadoPlatillo
import com.gastromind.app.data.repository.Pedido
import com.gastromind.app.data.repository.Resultado
import com.gastromind.app.ui.components.BotonTexto
import com.gastromind.app.ui.components.MensajeError
import com.gastromind.app.ui.pedidos.PlatilloComanda
import com.gastromind.app.ui.theme.Dimens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** En cocina solo se anula lo que ya se empezó: lo pendiente lo anula el mozo, sin merma. */
val sePuedeAnularEnCocina: (EstadoPlatillo) -> Boolean = {
    it == EstadoPlatillo.EN_PREPARACION || it == EstadoPlatillo.LISTO
}

// La BD guarda los motivos sin tildes (script_database.sql)
private val CON_TILDE = mapOf("DEVOLUCION" to "Devolución", "CORTESIA" to "Cortesía")

/** "MAL_MANEJO" → "Mal manejo". */
fun nombreDeMotivo(nombre: String) =
    CON_TILDE[nombre] ?: nombre.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

data class AnularUiState(
    val pedidoId: Long? = null,
    val mesa: Int = 0,
    val platillo: PlatilloComanda? = null,
    val motivos: List<MotivoMermaDto> = emptyList(),
    val motivoId: Long? = null,
    val guardando: Boolean = false,
    val error: String? = null
) {
    val abierto: Boolean get() = platillo != null
}

/**
 * HU-18 (CA-18): el cocinero anula un platillo que ya empezó. Pide el motivo; el backend registra sus insumos como
 * merma. Al terminar devuelve el pedido actualizado para que la comanda lo quite sin esperar el siguiente refresco.
 */
class AnularViewModel(private val repo: AnularRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AnularUiState())
    val uiState: StateFlow<AnularUiState> = _uiState.asStateFlow()

    fun abrir(pedidoId: Long, mesa: Int, platillo: PlatilloComanda) {
        if (!sePuedeAnularEnCocina(platillo.estado)) return
        _uiState.update {
            it.copy(pedidoId = pedidoId, mesa = mesa, platillo = platillo, motivoId = null, guardando = false, error = null)
        }
        if (_uiState.value.motivos.isEmpty()) {
            viewModelScope.launch {
                when (val r = repo.motivos()) {
                    is Resultado.Exito -> _uiState.update { estado ->
                        // Si el motivo PLATO_ANULADO existe, viene marcado: es el caso más común
                        val sugerido = r.valor.firstOrNull { it.nombre == MOTIVO_SUGERIDO }?.id
                        estado.copy(motivos = r.valor, motivoId = estado.motivoId ?: sugerido)
                    }
                    is Resultado.Error -> _uiState.update { it.copy(error = r.mensaje) }
                }
            }
        } else {
            _uiState.update { estado ->
                estado.copy(motivoId = estado.motivos.firstOrNull { it.nombre == MOTIVO_SUGERIDO }?.id)
            }
        }
    }

    fun cerrar() = _uiState.update { it.copy(pedidoId = null, platillo = null, guardando = false, error = null) }

    fun onMotivo(id: Long) = _uiState.update { it.copy(motivoId = id, error = null) }

    fun confirmar(onAnulado: (Pedido) -> Unit) {
        val estado = _uiState.value
        val pedido = estado.pedidoId ?: return
        val platillo = estado.platillo ?: return
        if (estado.guardando) return
        if (estado.motivoId == null) {
            _uiState.update { it.copy(error = "Elige el motivo.") }
            return
        }
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.anular(pedido, platillo.id, estado.motivoId)) {
                is Resultado.Exito -> {
                    cerrar()
                    onAnulado(r.valor)
                }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, error = r.mensaje) }
            }
        }
    }

    companion object {
        const val MOTIVO_SUGERIDO = "PLATO_ANULADO"

        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GastroMindApp
                AnularViewModel(app.container.anularRepository)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogoAnular(estado: AnularUiState, onMotivo: (Long) -> Unit, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    val platillo = estado.platillo ?: return
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Anular ${platillo.nombre} · M${estado.mesa}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                Text(
                    "Ya se empezó a preparar: sus insumos se registran como merma. ¿Por qué se anula?",
                    style = MaterialTheme.typography.bodyMedium
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    estado.motivos.forEach { motivo ->
                        FilterChip(
                            selected = motivo.id == estado.motivoId,
                            onClick = { onMotivo(motivo.id) },
                            label = { Text(nombreDeMotivo(motivo.nombre)) }
                        )
                    }
                }
                estado.error?.let { MensajeError(it) }
            }
        },
        confirmButton = { BotonTexto(if (estado.guardando) "Anulando..." else "Anular platillo", onClick = onConfirmar) },
        dismissButton = { BotonTexto("Volver", onClick = onCancelar) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
}
