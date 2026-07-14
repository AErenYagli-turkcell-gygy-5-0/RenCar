package com.turkcell.rencar.data.remote.reservation.dto

data class ReservationResponseDto(
    val id: String,
    val vehicleId: String,
    val vehicle: ReservationVehicleSummaryDto,
    val status: String,
    val expiresAt: String,
    val remainingSeconds: Int,
    val createdAt: String
)
