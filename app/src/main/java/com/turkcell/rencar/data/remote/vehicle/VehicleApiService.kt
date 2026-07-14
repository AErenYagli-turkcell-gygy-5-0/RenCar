package com.turkcell.rencar.data.remote.vehicle

import com.turkcell.rencar.data.remote.vehicle.dto.VehicleResponseDto
import com.turkcell.rencar.data.remote.vehicle.dto.QuoteResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApiService {

    @GET("vehicles")
    suspend fun list(@Query("type") type: String? = null): List<VehicleResponseDto>

    @GET("vehicles/{id}")
    suspend fun getOne(@Path("id") id: String): VehicleResponseDto

    @GET("vehicles/{id}/quote")
    suspend fun quote(
        @Path("id") id: String,
        @Query("plan") plan: String,
        @Query("minutes") minutes: Int
    ): QuoteResponseDto
}
