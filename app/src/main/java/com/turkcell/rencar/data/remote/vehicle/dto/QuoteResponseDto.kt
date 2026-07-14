package com.turkcell.rencar.data.remote.vehicle.dto

data class QuoteResponseDto(
    val vehicleId: String,
    val plan: String,
    val minutes: Int,
    val usageFee: Double,
    val startFee: Double,
    val serviceFee: Double,
    val estimatedTotal: Double
)
