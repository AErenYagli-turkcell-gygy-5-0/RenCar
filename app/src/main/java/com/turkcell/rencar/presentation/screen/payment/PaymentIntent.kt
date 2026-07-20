package com.turkcell.rencar.presentation.screen.payment

import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.rental.PaymentMethod
import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface PaymentIntent : UiIntent {
    data class ScreenStarted(val rentalId: String) : PaymentIntent
    data class MethodSelected(val method: PaymentMethod) : PaymentIntent
    data object ChangeCardClicked : PaymentIntent
    data class CardSelected(val cardId: String) : PaymentIntent
    data object CardPickerDismissed : PaymentIntent
    data object AddCardClicked : PaymentIntent
    data class AddCardBrandChanged(val brand: CardBrand) : PaymentIntent
    data class AddCardLast4Changed(val value: String) : PaymentIntent
    data class AddCardExpMonthChanged(val value: String) : PaymentIntent
    data class AddCardExpYearChanged(val value: String) : PaymentIntent
    data object AddCardConfirmClicked : PaymentIntent
    data object AddCardDismissed : PaymentIntent
    data object PayClicked : PaymentIntent
    data object IyzicoWebViewDismissed : PaymentIntent
    data object IyzicoPaymentCheckClicked : PaymentIntent
    data object TopUpConfirmed : PaymentIntent
    data object TopUpDismissed : PaymentIntent
}
