package com.turkcell.rencar.domain.wallet

sealed interface WalletError {
    data object InvalidRequest : WalletError
    data object Unauthorized : WalletError
    data object Forbidden : WalletError
    data object NotFound : WalletError
    data object Conflict : WalletError
    data object Network : WalletError
    data object Unexpected : WalletError
}
