package com.turkcell.rencar.data.remote.reservation

import com.turkcell.rencar.data.remote.reservation.dto.CreateReservationRequestDto
import com.turkcell.rencar.data.remote.reservation.dto.ReservationResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationApiService {
    @POST("reservations")
    suspend fun create(@Body request: CreateReservationRequestDto): ReservationResponseDto

    @GET("reservations/active")
    suspend fun getActive(): ReservationResponseDto

    @DELETE("reservations/{id}")
    suspend fun cancel(@Path("id") id: String)
}
