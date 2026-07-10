package com.turkcell.rencar.domain.vehicle

data class Vehicle(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: VehicleType,
    val pricePerDay: Double,
    val latitude: Double,
    val longitude: Double
)
