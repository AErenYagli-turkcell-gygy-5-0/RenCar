package com.turkcell.rencar.domain.wallet

sealed interface WalletResult<out T> {
    data class Success<T>(val data: T) : WalletResult<T>
    data class Failure(val error: WalletError) : WalletResult<Nothing>
}
