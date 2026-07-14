package com.turkcell.rencar.presentation.screen.rental.photo

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface RentalPhotoUploadEffect : UiEffect {
    data object NavigateBack : RentalPhotoUploadEffect
    data class NavigateToActiveRental(val rentalId: String, val vehicleId: String) : RentalPhotoUploadEffect
    data object NavigateHome : RentalPhotoUploadEffect
}
