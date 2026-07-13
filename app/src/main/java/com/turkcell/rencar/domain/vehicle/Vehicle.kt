package com.turkcell.rencar.domain.vehicle

data class Vehicle(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: VehicleType,
    val pricePerDay: Double,
    val pricePerMinute: Double,
    val pricePerHour: Double,
    val fuelPercent: Double,
    val rangeKm: Double,
    val transmission: Transmission,
    val seats: Int,
    val segment: VehicleSegment,
    val status: VehicleStatus,
    val latitude: Double,
    val longitude: Double
)
