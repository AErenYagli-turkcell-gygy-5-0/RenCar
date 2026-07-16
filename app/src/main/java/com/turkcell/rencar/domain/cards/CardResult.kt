package com.turkcell.rencar.domain.cards

sealed interface CardResult<out T> {
    data class Success<T>(val data: T) : CardResult<T>
    data class Failure(val error: CardError) : CardResult<Nothing>
}
