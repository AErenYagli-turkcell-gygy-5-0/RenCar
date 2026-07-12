package com.turkcell.rencar.domain.reservation

sealed interface ReservationError {
    data object InvalidRequest : ReservationError
    data object Unauthorized : ReservationError
    data object Forbidden : ReservationError
    data object NotFound : ReservationError
    data object Conflict : ReservationError
    data object Network : ReservationError
    data object Unexpected : ReservationError
}
