package com.gastromind.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class MensajeDeErrorTest {

    @Test
    fun usaElMensajeDelBackend() {
        val cuerpo = """{"error":"NO_AUTENTICADO","mensaje":"Credenciales inválidas"}"""
        assertEquals("Credenciales inválidas", mensajeDeError(401, cuerpo))
    }

    @Test
    fun enUnaValidacion_UsaElPrimerCampo() {
        val cuerpo = """{"error":"VALIDACION","mensaje":"Datos inválidos","campos":{"pin":"El PIN debe tener 4 dígitos"}}"""
        assertEquals("El PIN debe tener 4 dígitos", mensajeDeError(400, cuerpo))
    }

    @Test
    fun sinCuerpo_UsaUnoSegunElCodigo() {
        assertEquals("Demasiados intentos. Espera unos minutos.", mensajeDeError(429, null))
        assertEquals("El servidor tuvo un problema. Intenta de nuevo.", mensajeDeError(502, "<html>"))
    }
}
