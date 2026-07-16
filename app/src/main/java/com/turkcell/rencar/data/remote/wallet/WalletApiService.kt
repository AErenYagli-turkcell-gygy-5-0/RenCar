package com.turkcell.rencar.data.remote.wallet

import com.turkcell.rencar.data.remote.wallet.dto.TopupRequestDto
import com.turkcell.rencar.data.remote.wallet.dto.WalletResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WalletApiService {
    @GET("wallet")
    suspend fun getWallet(): WalletResponseDto

    @POST("wallet/topup")
    suspend fun topup(@Body request: TopupRequestDto): WalletResponseDto
}
