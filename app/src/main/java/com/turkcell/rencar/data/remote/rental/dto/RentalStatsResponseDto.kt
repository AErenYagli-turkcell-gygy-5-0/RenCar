package com.turkcell.rencar.data.remote.rental.dto

data class RentalStatsResponseDto(
    val tripCount: Int,
    val totalSpent: Double,
    val totalMinutes: Double,
    val totalKm: Double
)
