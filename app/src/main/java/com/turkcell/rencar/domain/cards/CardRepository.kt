package com.turkcell.rencar.domain.cards

interface CardRepository {
    suspend fun getCards(): CardResult<List<Card>>

    suspend fun addCard(
        brand: CardBrand,
        last4: String,
        expMonth: Int,
        expYear: Int
    ): CardResult<Card>

    suspend fun setDefaultCard(cardId: String): CardResult<Card>

    suspend fun deleteCard(cardId: String): CardResult<Unit>
}
