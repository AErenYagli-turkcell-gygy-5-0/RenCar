package com.turkcell.rencar.domain.wallet

data class Wallet(
    val id: String,
    val balance: Double,
    val transactions: List<WalletTransaction>
)

data class WalletTransaction(
    val id: String,
    val type: WalletTransactionType,
    val amount: Double,
    val rentalId: String?,
    val description: String,
    val createdAt: String
)

enum class WalletTransactionType {
    TOPUP,
    RENTAL_PAYMENT,
    REFERRAL_BONUS
}
