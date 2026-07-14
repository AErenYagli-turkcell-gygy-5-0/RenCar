package com.turkcell.rencar.presentation.screen.rental.photo

import android.net.Uri
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface RentalPhotoUploadIntent : UiIntent {
    data class ScreenStarted(
        val rentalId: String,
        val vehicleId: String,
        val mode: RentalPhotoUploadMode
    ) : RentalPhotoUploadIntent

    data object BackClicked : RentalPhotoUploadIntent
    data class PhotoSelected(val side: RentalPhotoSide, val uri: Uri) : RentalPhotoUploadIntent
    data object PrimaryActionClicked : RentalPhotoUploadIntent
}
