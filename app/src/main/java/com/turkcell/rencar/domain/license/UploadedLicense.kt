package com.turkcell.rencar.domain.license

data class UploadedLicense(
    val id: String,
    val status: String,
    val frontImageUrl: String,
    val backImageUrl: String
)
