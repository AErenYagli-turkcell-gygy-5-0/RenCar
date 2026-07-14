package com.turkcell.rencar.data.remote.rental.dto

data class RentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val plan: String,
    val startDate: String,
    val endDate: String?,
    val totalPrice: Double?,
    val status: String,
    val createdAt: String
)
