package com.gastromind.app.data.repository

/** Lo que devuelve un repositorio a su ViewModel: el dato o un mensaje listo para mostrar. */
sealed interface Resultado<out T> {
    data class Exito<T>(val valor: T) : Resultado<T>
    data class Error(val mensaje: String) : Resultado<Nothing>
}
