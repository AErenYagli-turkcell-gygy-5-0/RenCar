package com.turkcell.rencar.domain.auth

sealed interface AuthError {
    data object EmailAlreadyRegistered : AuthError
    data object UserNotFound : AuthError
    data object InvalidOtp : AuthError
    data object InvalidReferralCode : AuthError
    data object Unauthorized : AuthError
    data object Network : AuthError
    data object Unexpected : AuthError
}
