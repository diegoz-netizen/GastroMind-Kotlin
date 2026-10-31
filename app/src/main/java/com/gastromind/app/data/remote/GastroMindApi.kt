package com.gastromind.app.data.remote

import com.gastromind.app.data.remote.dto.AuthResponse
import com.gastromind.app.data.remote.dto.CompraOpcionesResponse
import com.gastromind.app.data.remote.dto.IngresoCompraRequest
import com.gastromind.app.data.remote.dto.IngresoCompraResponse
import com.gastromind.app.data.remote.dto.LoginPinRequest
import com.gastromind.app.data.remote.dto.LoginRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Endpoints del backend Spring. Cada HU agrega los suyos (pedidos, comandas, ingresos…). */
interface GastroMindApi {

    /** HU-02: correo y contraseña. */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /** HU-27: correo y PIN de 4 dígitos. Mismo JWT que el login web. */
    @POST("api/auth/login-pin")
    suspend fun loginPin(@Body request: LoginPinRequest): AuthResponse

    /** HU-14: proveedores e insumos del restaurante para el formulario. */
    @GET("api/compras/opciones")
    suspend fun opcionesDeCompra(): CompraOpcionesResponse

    /** HU-14: registra la compra y sube el stock. */
    @POST("api/compras")
    suspend fun registrarIngreso(@Body request: IngresoCompraRequest): IngresoCompraResponse
}
