package com.turkcell.rencar.data.remote.rental.dto

data class CreateRentalRequestDto(
    val vehicleId: String,
    val plan: String,
    val endDate: String? = null
)
