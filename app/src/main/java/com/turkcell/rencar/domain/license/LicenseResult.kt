package com.turkcell.rencar.domain.license

sealed interface LicenseResult<out T> {
    data class Success<T>(val data: T) : LicenseResult<T>
    data class Failure(val error: LicenseError) : LicenseResult<Nothing>
}
