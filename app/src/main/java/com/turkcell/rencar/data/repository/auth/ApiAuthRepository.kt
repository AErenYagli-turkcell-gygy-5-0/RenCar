package com.turkcell.rencar.data.repository.auth

import com.turkcell.rencar.data.remote.auth.AuthApiService
import com.turkcell.rencar.data.remote.auth.dto.LoginRequestDto
import com.turkcell.rencar.data.remote.auth.dto.RefreshTokenRequestDto
import com.turkcell.rencar.data.remote.auth.dto.RegisterRequestDto
import com.turkcell.rencar.data.remote.auth.dto.UserResponseDto
import com.turkcell.rencar.data.remote.auth.dto.VerifyOtpRequestDto
import com.turkcell.rencar.data.session.SessionTokenHolder
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import com.turkcell.rencar.domain.auth.VerifiedSession
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiAuthRepository @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionTokenHolder: SessionTokenHolder
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): AuthResult<RegisteredUser> =
        runRequest(
            httpError = { code ->
                if (code == HTTP_CONFLICT) {
                    AuthError.EmailAlreadyRegistered
                } else {
                    AuthError.Unexpected
                }
            }
        ) {
            val response = apiService.register(
                RegisterRequestDto(
                    email = request.email,
                    password = request.password,
                    fullName = request.fullName,
                    phone = request.phone
                )
            )
            response.user.toDomain()
        }

    override suspend fun requestLogin(phone: String): AuthResult<LoginChallenge> =
        runRequest(
            httpError = { code ->
                if (code == HTTP_UNAUTHORIZED) {
                    AuthError.UserNotFound
                } else {
                    AuthError.Unexpected
                }
            }
        ) {
            val response = apiService.login(LoginRequestDto(phone = phone))
            LoginChallenge(
                message = response.message,
                phone = response.phone,
                expiresAt = response.expiresAt
            )
        }

    override suspend fun verifyOtp(phone: String, code: String): AuthResult<VerifiedSession> =
        runRequest(
            httpError = { statusCode ->
                if (statusCode == HTTP_UNAUTHORIZED) {
                    AuthError.InvalidOtp
                } else {
                    AuthError.Unexpected
                }
            }
        ) {
            val response = apiService.verifyOtp(VerifyOtpRequestDto(phone = phone, code = code))
            response.toSession()
        }

    override suspend fun refreshSession(): AuthResult<VerifiedSession> {
        val refreshToken = sessionTokenHolder.refreshToken
            ?: return AuthResult.Failure(AuthError.Unexpected)

        return runRequest(httpError = { AuthError.Unexpected }) {
            apiService.refresh(RefreshTokenRequestDto(refreshToken)).toSession()
        }
    }

    override suspend fun getCurrentUser(): AuthResult<RegisteredUser> =
        runRequest(httpError = { code ->
            if (code == HTTP_UNAUTHORIZED) {
                AuthError.Unauthorized
            } else {
                AuthError.Unexpected
            }
        }) {
            apiService.me().toDomain()
        }

    override suspend fun logout(): AuthResult<Unit> =
        runRequest(httpError = { code ->
            if (code == HTTP_UNAUTHORIZED) {
                AuthError.Unauthorized
            } else {
                AuthError.Unexpected
            }
        }) {
            apiService.logout()
            sessionTokenHolder.clear()
        }

    private suspend fun <T> runRequest(
        httpError: (Int) -> AuthError,
        request: suspend () -> T
    ): AuthResult<T> = try {
        AuthResult.Success(request())
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        AuthResult.Failure(httpError(error.code()))
    } catch (error: IOException) {
        AuthResult.Failure(AuthError.Network)
    } catch (error: Exception) {
        AuthResult.Failure(AuthError.Unexpected)
    }

    private fun UserResponseDto.toDomain() = RegisteredUser(
        id = id,
        email = email,
        phone = phone.orEmpty(),
        fullName = fullName,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun com.turkcell.rencar.data.remote.auth.dto.AuthResponseDto.toSession(): VerifiedSession {
        sessionTokenHolder.update(accessToken = accessToken, refreshToken = refreshToken)
        return VerifiedSession(
            user = user.toDomain(),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_CONFLICT = 409
    }
}
