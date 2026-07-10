package com.turkcell.rencar.data.remote.vehicle

import com.turkcell.rencar.data.remote.vehicle.dto.VehicleResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleApiService {

    @GET("vehicles")
    suspend fun list(@Query("type") type: String? = null): List<VehicleResponseDto>

    @GET("vehicles/{id}")
    suspend fun getOne(@Path("id") vehicleId: String): VehicleResponseDto
}
