package com.turkcell.rencar.domain.cards

sealed interface CardError {
    data object InvalidRequest : CardError
    data object Unauthorized : CardError
    data object Forbidden : CardError
    data object NotFound : CardError
    data object Conflict : CardError
    data object Network : CardError
    data object Unexpected : CardError
}
