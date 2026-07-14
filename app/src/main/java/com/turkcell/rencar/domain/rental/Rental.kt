package com.turkcell.rencar.domain.rental

data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val plan: RentalPlan,
    val startDate: String,
    val endDate: String?,
    val totalPrice: Double?,
    val status: String,
    val createdAt: String
)
