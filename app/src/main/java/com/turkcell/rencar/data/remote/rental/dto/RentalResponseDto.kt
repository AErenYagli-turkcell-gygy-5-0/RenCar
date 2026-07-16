package com.turkcell.rencar.data.remote.rental.dto

data class RentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryResponseDto,
    val plan: String,
    val startDate: String,
    val startedAt: String,
    val endedAt: String?,
    val endDate: String?,
    val totalPrice: Double?,
    val startFee: Double,
    val serviceFee: Double?,
    val distanceKm: Double,
    val durationMinutes: Double,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String?,
    val discountAmount: Double,
    val createdAt: String
)
