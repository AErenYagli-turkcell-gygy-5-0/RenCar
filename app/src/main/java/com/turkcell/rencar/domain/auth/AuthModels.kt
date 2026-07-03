package com.turkcell.rencar.domain.auth

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String
)

data class RegisteredUser(
    val id: String,
    val email: String,
    val phone: String,
    val fullName: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)

data class LoginChallenge(
    val message: String,
    val phone: String,
    val expiresAt: String
)

sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}

sealed interface AuthError {
    data object EmailAlreadyRegistered : AuthError
    data object UserNotFound : AuthError
    data object Network : AuthError
    data object Unexpected : AuthError
}
