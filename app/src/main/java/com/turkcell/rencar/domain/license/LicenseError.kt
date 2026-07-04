package com.turkcell.rencar.domain.license

sealed interface LicenseError {
    data object Unauthorized : LicenseError
    data object InvalidFile : LicenseError
    data object FileTooLarge : LicenseError
    data object AlreadyReviewedOrCustomer : LicenseError
    data object Network : LicenseError
    data object Unexpected : LicenseError
}
