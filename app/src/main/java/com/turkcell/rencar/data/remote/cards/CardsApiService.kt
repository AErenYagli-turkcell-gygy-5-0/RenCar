package com.turkcell.rencar.data.remote.cards

import com.turkcell.rencar.data.remote.cards.dto.CardResponseDto
import com.turkcell.rencar.data.remote.cards.dto.CreateCardRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CardsApiService {
    @GET("cards")
    suspend fun list(): List<CardResponseDto>

    @POST("cards")
    suspend fun create(@Body request: CreateCardRequestDto): CardResponseDto

    @PATCH("cards/{id}/default")
    suspend fun setDefault(@Path("id") id: String): CardResponseDto

    @DELETE("cards/{id}")
    suspend fun delete(@Path("id") id: String)
}
