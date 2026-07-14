package com.turkcell.rencar.domain.reservation

import com.turkcell.rencar.domain.vehicle.VehicleType

data class ReservationVehicleSummary(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: VehicleType,
    val latitude: Double,
    val longitude: Double,
    val pricePerMinute: Double
)
