package com.turkcell.rencar.domain.rental

data class PaymentReceipt(
    val rentalId: String,
    val paymentStatus: PaymentStatus,
    val method: PaymentMethod,
    val totalPrice: Double,
    val discountAmount: Double,
    val paidAmount: Double,
    val walletBalance: Double?,
    val cardBrand: String?,
    val cardLast4: String?
)
