package com.turkcell.rencar.domain.vehicle

data class Vehicle(
    val id: String,
    val type: VehicleType,
    val pricePerDay: Double,
    val latitude: Double,
    val longitude: Double
)
