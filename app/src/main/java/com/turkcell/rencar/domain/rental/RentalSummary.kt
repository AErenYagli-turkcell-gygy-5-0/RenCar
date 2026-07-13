package com.turkcell.rencar.domain.rental

data class RentalSummary(
    val id: String,
    val vehicleId: String,
    val status: RentalStatus
)
