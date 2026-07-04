package com.turkcell.rencar.data.remote.license.dto

data class LicenseStatusResponseDto(
    val status: String,
    val frontImageUrl: String?,
    val backImageUrl: String?,
    val rejectReason: String?,
    val reviewedAt: String?
)
