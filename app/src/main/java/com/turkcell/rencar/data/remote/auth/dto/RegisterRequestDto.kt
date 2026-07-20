package com.turkcell.rencar.data.remote.auth.dto

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String,
    val referralCode: String? = null
)
