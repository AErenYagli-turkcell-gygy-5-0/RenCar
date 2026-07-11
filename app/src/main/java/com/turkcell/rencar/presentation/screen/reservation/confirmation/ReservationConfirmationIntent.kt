package com.turkcell.rencar.presentation.screen.reservation.confirmation

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface ReservationConfirmationIntent : UiIntent {
    data class ScreenStarted(val vehicleId: String) : ReservationConfirmationIntent
    data object BackClicked : ReservationConfirmationIntent
    data object RetryClicked : ReservationConfirmationIntent
    data class TermsAcceptanceChanged(val accepted: Boolean) : ReservationConfirmationIntent
    data object CompleteReservationClicked : ReservationConfirmationIntent
}
