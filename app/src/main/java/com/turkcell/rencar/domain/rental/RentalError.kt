package com.turkcell.rencar.domain.rental

sealed interface RentalError {
    data object InvalidRequest : RentalError
    data object Unauthorized : RentalError
    data object Forbidden : RentalError
    data object NotFound : RentalError
    data object Conflict : RentalError
    data object Network : RentalError
    data object Unexpected : RentalError
}
