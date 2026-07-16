package com.turkcell.rencar.data.remote.rental.dto

data class RentalHistoryItemResponseDto(
    val id: String,
    val vehicle: RentalVehicleSummaryResponseDto,
    val totalPrice: Double?,
    val distanceKm: Double,
    val durationMinutes: Double,
    val status: String,
    val startedAt: String
)
