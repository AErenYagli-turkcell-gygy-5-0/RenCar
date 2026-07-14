package com.turkcell.rencar.domain.rental

data class ActiveRental(
    val id: String,
    val vehicleId: String,
    val status: RentalStatus,
    val elapsedSeconds: Long,
    val currentCost: Double,
    val distanceKm: Double
)
