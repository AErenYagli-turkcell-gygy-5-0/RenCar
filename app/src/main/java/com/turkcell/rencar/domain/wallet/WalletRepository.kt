package com.turkcell.rencar.domain.wallet

interface WalletRepository {
    suspend fun getWallet(): WalletResult<Wallet>

    suspend fun topUp(amount: Double): WalletResult<Wallet>
}
