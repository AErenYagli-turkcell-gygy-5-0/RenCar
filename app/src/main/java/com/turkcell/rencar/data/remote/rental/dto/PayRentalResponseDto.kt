package com.turkcell.rencar.data.remote.rental.dto

data class PayRentalResponseDto(
    val rentalId: String,
    val paymentStatus: String,
    val method: String,
    val totalPrice: Double,
    val discountAmount: Double,
    val paidAmount: Double,
    val walletBalance: Double?,
    val card: PaidCardSummaryResponseDto?
)

data class PaidCardSummaryResponseDto(
    val brand: String,
    val last4: String
)
