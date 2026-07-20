package com.turkcell.rencar.domain.auth

data class RegisteredUser(
    val id: String,
    val email: String,
    val phone: String,
    val fullName: String,
    val role: String,
    val referralCode: String?,
    val createdAt: String,
    val updatedAt: String
)
