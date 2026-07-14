package com.turkcell.rencar.domain.rental

import com.turkcell.rencar.domain.vehicle.VehicleType

data class RentalHistoryItem(
    val id: String,
    val vehicleBrand: String,
    val vehicleModel: String,
    val vehiclePlate: String,
    val vehicleType: VehicleType,
    val totalPrice: Double?,
    val distanceKm: Double,
    val durationMinutes: Double,
    val startedAt: String
)
