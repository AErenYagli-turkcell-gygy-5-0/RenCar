package com.turkcell.rencar.data.remote.cards.dto

data class CardResponseDto(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean,
    val createdAt: String
)
