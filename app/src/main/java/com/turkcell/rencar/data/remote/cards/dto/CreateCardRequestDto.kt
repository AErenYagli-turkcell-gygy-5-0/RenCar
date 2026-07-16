package com.turkcell.rencar.data.remote.cards.dto

data class CreateCardRequestDto(
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int
)
