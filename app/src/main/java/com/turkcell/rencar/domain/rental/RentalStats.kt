package com.turkcell.rencar.domain.rental

data class RentalStats(
    val tripCount: Int,
    val totalSpent: Double,
    val totalMinutes: Double,
    val totalKm: Double
)
