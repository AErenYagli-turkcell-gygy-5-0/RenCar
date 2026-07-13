package com.turkcell.rencar.data.remote.rental

import com.turkcell.rencar.data.remote.rental.dto.CreateRentalRequestDto
import com.turkcell.rencar.data.remote.rental.dto.RentalResponseDto
import com.turkcell.rencar.data.remote.rental.dto.RentalSummaryResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RentalApiService {
    @POST("rentals")
    suspend fun create(@Body request: CreateRentalRequestDto): RentalResponseDto

    @GET("rentals")
    suspend fun listMine(): List<RentalSummaryResponseDto>
}
