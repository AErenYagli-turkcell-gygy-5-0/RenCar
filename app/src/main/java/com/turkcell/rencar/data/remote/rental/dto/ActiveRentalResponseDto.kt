package com.turkcell.rencar.data.remote.rental.dto

data class ActiveRentalResponseDto(
    val id: String,
    val vehicleId: String,
    val status: String,
    val plan: String,
    val startFee: Double,
    val startedAt: String,
    val elapsedSeconds: Long,
    val currentCost: Double,
    val distanceKm: Double
)
