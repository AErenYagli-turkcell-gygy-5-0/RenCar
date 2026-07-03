package com.turkcell.rencar.data.remote

import com.turkcell.rencar.data.remote.dto.AuthResponseDto
import com.turkcell.rencar.data.remote.dto.LoginRequestDto
import com.turkcell.rencar.data.remote.dto.LoginResponseDto
import com.turkcell.rencar.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}
