package com.turkcell.rencar.domain.vehicle

sealed interface VehicleError {
    data object Unauthorized : VehicleError
    data object Forbidden : VehicleError
    data object Network : VehicleError
    data object Unexpected : VehicleError
}
