package com.turkcell.rencar.data.remote.reservation.dto

data class ReservationVehicleSummaryDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val pricePerMinute: Double
)
