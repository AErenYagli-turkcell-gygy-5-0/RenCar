package com.turkcell.rencar.domain.auth

data class VerifiedSession(
    val user: RegisteredUser,
    val accessToken: String,
    val refreshToken: String
)
