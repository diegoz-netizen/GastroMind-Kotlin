package com.gastromind.app.data.remote

import com.gastromind.app.data.remote.dto.InsumoDto
import com.gastromind.app.data.remote.dto.KardexResponse
import com.gastromind.app.data.remote.dto.ProductoDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints del backend Django (catálogo y kardex), con el mismo JWT de Spring. */
interface DjangoApi {

    @GET("api/catalogo/insumos/")
    suspend fun insumos(): List<InsumoDto>

    /** HU-09: la carta del restaurante. Para tomar pedidos se pide solo lo ACTIVO. */
    @GET("api/catalogo/productos/")
    suspend fun productos(@Query("estado") estado: String): List<ProductoDto>

    /** HU-15: fechas en AAAA-MM-DD, las dos opcionales. */
    @GET("api/catalogo/insumos/{id}/kardex/")
    suspend fun kardex(
        @Path("id") insumoId: Long,
        @Query("desde") desde: String?,
        @Query("hasta") hasta: String?
    ): KardexResponse
}
