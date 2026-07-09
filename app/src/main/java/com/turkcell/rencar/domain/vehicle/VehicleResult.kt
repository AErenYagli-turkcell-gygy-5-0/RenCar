package com.turkcell.rencar.domain.vehicle

sealed interface VehicleResult<out T> {
    data class Success<T>(val data: T) : VehicleResult<T>
    data class Failure(val error: VehicleError) : VehicleResult<Nothing>
}
