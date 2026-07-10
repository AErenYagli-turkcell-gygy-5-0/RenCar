package com.turkcell.rencar.domain.rental

sealed interface RentalResult<out T> {
    data class Success<T>(val data: T) : RentalResult<T>
    data class Failure(val error: RentalError) : RentalResult<Nothing>
}
