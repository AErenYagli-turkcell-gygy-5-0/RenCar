package com.turkcell.rencar.domain.auth

data class LoginChallenge(
    val message: String,
    val phone: String,
    val expiresAt: String
)
