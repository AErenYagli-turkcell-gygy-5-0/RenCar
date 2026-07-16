package com.turkcell.rencar.data.remote.wallet.dto

data class WalletTransactionResponseDto(
    val id: String,
    val type: String,
    val amount: Double,
    val rentalId: String?,
    val description: String,
    val createdAt: String
)
