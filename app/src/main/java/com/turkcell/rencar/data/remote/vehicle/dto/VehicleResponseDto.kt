package com.turkcell.rencar.data.remote.vehicle.dto

data class VehicleResponseDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val updatedAt: String
)
