package com.turkcell.rencar.data.remote.wallet.dto

data class WalletResponseDto(
    val id: String,
    val balance: Double,
    val transactions: List<WalletTransactionResponseDto>
)
