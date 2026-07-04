package com.turkcell.rencar.data.remote.auth.dto

data class LoginResponseDto(
    val message: String,
    val phone: String,
    val expiresAt: String
)
