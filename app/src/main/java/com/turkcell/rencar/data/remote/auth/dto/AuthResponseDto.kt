package com.turkcell.rencar.data.remote.auth.dto

data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponseDto
)
