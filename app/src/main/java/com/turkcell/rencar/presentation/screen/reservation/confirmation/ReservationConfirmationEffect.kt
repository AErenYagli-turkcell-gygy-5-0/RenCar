package com.turkcell.rencar.presentation.screen.reservation.confirmation

import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface ReservationConfirmationEffect : UiEffect {
    data object NavigateBack : ReservationConfirmationEffect
    data class ReservationCreated(
        val vehicleId: String,
        val rentalPlan: RentalPlan
    ) : ReservationConfirmationEffect
}
