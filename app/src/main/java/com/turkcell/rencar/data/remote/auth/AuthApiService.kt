package com.turkcell.rencar.data.remote.auth

import com.turkcell.rencar.data.remote.auth.dto.AuthResponseDto
import com.turkcell.rencar.data.remote.auth.dto.LoginRequestDto
import com.turkcell.rencar.data.remote.auth.dto.LoginResponseDto
import com.turkcell.rencar.data.remote.auth.dto.RefreshTokenRequestDto
import com.turkcell.rencar.data.remote.auth.dto.RegisterRequestDto
import com.turkcell.rencar.data.remote.auth.dto.VerifyOtpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequestDto): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): AuthResponseDto
}
