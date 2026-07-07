package com.turkcell.rencar.domain.auth

interface AuthRepository {

    suspend fun register(request: RegisterRequest): AuthResult<RegisteredUser>

    suspend fun requestLogin(phone: String): AuthResult<LoginChallenge>

    suspend fun verifyOtp(phone: String, code: String): AuthResult<VerifiedSession>

    suspend fun refreshSession(): AuthResult<VerifiedSession>
}
