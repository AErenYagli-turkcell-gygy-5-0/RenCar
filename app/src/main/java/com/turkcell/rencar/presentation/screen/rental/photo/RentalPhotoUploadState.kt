package com.turkcell.rencar.presentation.screen.rental.photo

import android.net.Uri
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.presentation.core.mvi.UiState

enum class RentalPhotoUploadMode {
    START_TRIP,
    RETURN_TRIP
}

data class RentalPhotoUploadState(
    val rentalId: String = "",
    val vehicleId: String = "",
    val mode: RentalPhotoUploadMode = RentalPhotoUploadMode.START_TRIP,
    val vehicleName: String = "",
    val plate: String = "",
    val photos: Map<RentalPhotoSide, Uri> = emptyMap(),
    val uploadedSides: Set<RentalPhotoSide> = emptySet(),
    val isLoading: Boolean = false,
    val uploadingSide: RentalPhotoSide? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) : UiState {

    val completedSides: Set<RentalPhotoSide>
        get() = if (mode == RentalPhotoUploadMode.START_TRIP) uploadedSides else photos.keys

    val remainingCount: Int
        get() = RentalPhotoSide.entries.size - completedSides.size

    val allPhotosReady: Boolean
        get() = remainingCount <= 0

    val canSubmit: Boolean
        get() = allPhotosReady && !isSubmitting && uploadingSide == null
}
