package com.turkcell.rencar.data.remote.rental.dto

data class RentalVehicleSummaryResponseDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String
)
