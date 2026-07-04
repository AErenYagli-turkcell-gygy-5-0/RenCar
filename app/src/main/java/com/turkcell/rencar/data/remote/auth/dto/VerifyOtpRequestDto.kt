package com.turkcell.rencar.data.remote.auth.dto

data class VerifyOtpRequestDto(
    val phone: String,
    val code: String
)
