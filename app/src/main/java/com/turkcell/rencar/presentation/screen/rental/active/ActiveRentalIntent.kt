package com.turkcell.rencar.presentation.screen.rental.active

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface ActiveRentalIntent : UiIntent {
    data class ScreenStarted(val rentalId: String, val vehicleId: String) : ActiveRentalIntent
    data object FinishClicked : ActiveRentalIntent
    data object BackClicked : ActiveRentalIntent
}
