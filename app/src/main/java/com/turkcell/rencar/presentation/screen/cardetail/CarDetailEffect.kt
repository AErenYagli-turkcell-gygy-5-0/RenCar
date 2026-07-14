package com.turkcell.rencar.presentation.screen.cardetail

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface CarDetailEffect : UiEffect {
    data object NavigateBack : CarDetailEffect
    data class NavigateToReservationConfirmation(val vehicleId: String) : CarDetailEffect
    data class NavigateToRentalPhotoUpload(val rentalId: String, val vehicleId: String) : CarDetailEffect
    data class NavigateToActiveRental(val rentalId: String, val vehicleId: String) : CarDetailEffect
}
