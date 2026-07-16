package com.turkcell.rencar.presentation.screen.payment

import com.turkcell.rencar.domain.cards.Card
import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.rental.PaymentMethod
import com.turkcell.rencar.presentation.core.mvi.UiState

data class PaymentState(
    val rentalId: String = "",
    val vehicleName: String = "",
    val vehiclePlate: String = "",
    val durationMinutes: Double = 0.0,
    val distanceKm: Double = 0.0,
    val usageFee: Double = 0.0,
    val startFee: Double = 0.0,
    val serviceFee: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalPrice: Double = 0.0,
    val isPaid: Boolean = false,
    val selectedMethod: PaymentMethod = PaymentMethod.WALLET,
    val walletBalance: Double = 0.0,
    val cards: List<Card> = emptyList(),
    val selectedCardId: String? = null,
    val showCardPicker: Boolean = false,
    val showInsufficientBalanceDialog: Boolean = false,
    val showAddCardDialog: Boolean = false,
    val addCardBrand: CardBrand = CardBrand.VISA,
    val addCardLast4Input: String = "",
    val addCardExpMonthInput: String = "",
    val addCardExpYearInput: String = "",
    val isAddCardSubmitting: Boolean = false,
    val addCardErrorMessage: String? = null,
    val isLoading: Boolean = false,
    val isPaying: Boolean = false,
    val errorMessage: String? = null
) : UiState {
    val netAmount: Double
        get() = (totalPrice - discountAmount).coerceAtLeast(0.0)

    val selectedCard: Card?
        get() = cards.firstOrNull { it.id == selectedCardId }
}
