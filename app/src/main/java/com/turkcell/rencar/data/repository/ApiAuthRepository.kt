package com.turkcell.rencar.data.repository

import com.turkcell.rencar.data.remote.AuthApiService
import com.turkcell.rencar.data.remote.dto.LoginRequestDto
import com.turkcell.rencar.data.remote.dto.RegisterRequestDto
import com.turkcell.rencar.data.remote.dto.UserResponseDto
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ApiAuthRepository @Inject constructor(
    private val apiService: AuthApiService
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
        phone = phone,
        fullName = fullName,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_CONFLICT = 409
    }
}
