package com.turkcell.rencar.data.remote.rental.dto

data class RentalPhotosStateResponseDto(
    val rentalId: String,
    val photos: List<RentalPhotoDto>,
    val uploadedCount: Int,
    val remainingSides: List<String>,
    val photosComplete: Boolean
)
