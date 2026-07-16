package com.turkcell.rencar.presentation.screen.rental.active

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface ActiveRentalEffect : UiEffect {
    data class NavigateToFinishPhotoUpload(val rentalId: String, val vehicleId: String) : ActiveRentalEffect
    data object NavigateToHome : ActiveRentalEffect
}
