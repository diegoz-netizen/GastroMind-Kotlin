package com.gastromind.app.ui.login

import com.gastromind.app.data.session.EmpleadoRecordado

enum class ModoIngreso { PIN, CORREO }

data class LoginUiState(
    val modo: ModoIngreso = ModoIngreso.PIN,
    /** El último empleado que entró aquí: con él solo se pide el PIN. */
    val recordado: EmpleadoRecordado? = null,
    val correo: String = "",
    val password: String = "",
    val pin: String = "",
    val cargando: Boolean = false,
    val error: String? = null
) {
    /** Con quién se intenta entrar por PIN: el recordado o el correo escrito. */
    val correoParaPin: String get() = recordado?.correo ?: correo
}
