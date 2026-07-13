package com.turkcell.rencar.domain.reservation

sealed interface ReservationResult<out T> {
    data class Success<T>(val data: T) : ReservationResult<T>
    data class Failure(val error: ReservationError) : ReservationResult<Nothing>
}
