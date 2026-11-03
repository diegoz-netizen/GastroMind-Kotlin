package com.gastromind.app.data.remote

import com.gastromind.app.data.remote.dto.AgregarPlatillosRequest
import com.gastromind.app.data.remote.dto.AuthResponse
import com.gastromind.app.data.remote.dto.CambioEstadoRequest
import com.gastromind.app.data.remote.dto.CompraOpcionesResponse
import com.gastromind.app.data.remote.dto.CrearPedidoRequest
import com.gastromind.app.data.remote.dto.IngresoCompraRequest
import com.gastromind.app.data.remote.dto.IngresoCompraResponse
import com.gastromind.app.data.remote.dto.LoginPinRequest
import com.gastromind.app.data.remote.dto.LoginRequest
import com.gastromind.app.data.remote.dto.MesaDto
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

    /** HU-09: mesas del restaurante, libres u ocupadas. */
    @GET("api/mesas")
    suspend fun mesas(): List<MesaDto>

    /** HU-09 (CA-09): pedido nuevo en una mesa libre; la mesa queda ocupada. */
    @POST("api/pedidos")
    suspend fun crearPedido(@Body request: CrearPedidoRequest): PedidoResponse

    /** HU-09, segunda ronda: platillos nuevos en el pedido abierto de la mesa. */
    @POST("api/pedidos/{pedidoId}/detalles")
    suspend fun agregarPlatillos(
        @Path("pedidoId") pedidoId: Long,
        @Body request: AgregarPlatillosRequest
    ): PedidoResponse

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

    /** HU-14: proveedores e insumos del restaurante para el formulario. */
    @GET("api/compras/opciones")
    suspend fun opcionesDeCompra(): CompraOpcionesResponse

    /** HU-14: registra la compra y sube el stock. */
    @POST("api/compras")
    suspend fun registrarIngreso(@Body request: IngresoCompraRequest): IngresoCompraResponse
}
