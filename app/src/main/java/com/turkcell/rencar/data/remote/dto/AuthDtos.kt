package com.turkcell.rencar.data.remote.dto

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String
)

data class LoginRequestDto(
    val phone: String
)

data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponseDto
)

data class UserResponseDto(
    val id: String,
    val email: String,
    val phone: String,
    val fullName: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)

data class LoginResponseDto(
    val message: String,
    val phone: String,
    val expiresAt: String
)
