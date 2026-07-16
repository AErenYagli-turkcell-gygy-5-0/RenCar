package com.turkcell.rencar.domain.cards

data class Card(
    val id: String,
    val brand: CardBrand,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean,
    val createdAt: String
)

enum class CardBrand {
    VISA,
    MASTERCARD
}
