package com.turkcell.rencar.data.remote.license.dto

data class LicenseResponseDto(
    val id: String,
    val status: String,
    val frontImageUrl: String,
    val backImageUrl: String,
    val rejectReason: String?,
    val reviewedAt: String?,
    val createdAt: String,
    val updatedAt: String
)
