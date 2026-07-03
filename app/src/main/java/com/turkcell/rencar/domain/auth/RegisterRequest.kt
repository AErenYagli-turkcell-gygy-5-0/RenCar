package com.turkcell.rencar.domain.auth

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String
)
