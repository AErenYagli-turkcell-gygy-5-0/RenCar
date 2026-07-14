package com.turkcell.rencar.domain.rental

data class RentalPhotosState(
    val rentalId: String,
    val uploadedSides: Set<RentalPhotoSide>,
    val remainingSides: Set<RentalPhotoSide>,
    val photosComplete: Boolean
)
