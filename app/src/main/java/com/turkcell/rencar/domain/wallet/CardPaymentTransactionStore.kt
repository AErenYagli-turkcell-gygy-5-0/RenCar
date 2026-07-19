package com.turkcell.rencar.domain.wallet

interface CardPaymentTransactionStore {
    fun getTransactions(): List<WalletTransaction>

    fun saveTransaction(transaction: WalletTransaction)
}
