package com.turkcell.rencar.domain.rental

data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicleBrand: String = "",
    val vehicleModel: String = "",
    val vehiclePlate: String = "",
    val plan: RentalPlan,
    val startDate: String,
    val endDate: String?,
    val totalPrice: Double?,
    val startFee: Double = 0.0,
    val serviceFee: Double? = null,
    val distanceKm: Double = 0.0,
    val durationMinutes: Double = 0.0,
    val status: String,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paymentMethod: PaymentMethod? = null,
    val discountAmount: Double = 0.0,
    val createdAt: String
)
