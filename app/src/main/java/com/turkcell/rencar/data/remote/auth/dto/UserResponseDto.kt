package com.turkcell.rencar.data.remote.auth.dto

data class UserResponseDto(
    val id: String,
    val email: String,
    val phone: String?,
    val fullName: String,
    val role: String,
    val referralCode: String?,
    val createdAt: String,
    val updatedAt: String
)
