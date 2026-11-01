package com.gastromind.app.data.remote

import com.gastromind.app.data.remote.dto.AuthResponse
import com.gastromind.app.data.remote.dto.CambioEstadoRequest
import com.gastromind.app.data.remote.dto.LoginPinRequest
import com.gastromind.app.data.remote.dto.LoginRequest
import com.gastromind.app.data.remote.dto.PedidoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoints del backend Spring. Cada HU agrega los suyos (pedidos, comandas, ingresos…). */
interface GastroMindApi {

    /** HU-02: correo y contraseña. */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /** HU-27: correo y PIN de 4 dígitos. Mismo JWT que el login web. */
    @POST("api/auth/login-pin")
    suspend fun loginPin(@Body request: LoginPinRequest): AuthResponse

    /** HU-10: pedidos sin cerrar del restaurante, del más antiguo al más nuevo. */
    @GET("api/pedidos")
    suspend fun pedidosVigentes(): List<PedidoResponse>

    /** HU-11: un paso del platillo. El backend valida el orden y que el rol pueda darlo. */
    @PATCH("api/pedidos/{pedidoId}/detalles/{detalleId}/estado")
    suspend fun cambiarEstadoPlatillo(
        @Path("pedidoId") pedidoId: Long,
        @Path("detalleId") detalleId: Long,
        @Body request: CambioEstadoRequest
    ): PedidoResponse
}
