package com.turkcell.rencar.data.remote.rental.dto

data class PayRentalRequestDto(
    val method: String,
    val cardId: String? = null,
    val discountCode: String? = null,
    val iyzicoPaymentId: String? = null
)
