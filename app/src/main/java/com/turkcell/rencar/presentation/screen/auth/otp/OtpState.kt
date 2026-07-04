package com.turkcell.rencar.presentation.screen.auth.otp

import com.turkcell.rencar.presentation.core.mvi.UiState

const val OTP_LENGTH = 6
const val OTP_VALIDITY_SECONDS = 300

data class OtpState(
    val phoneNumber: String = "+90 532 000 00 00",
    val digits: String = "",
    val remainingSeconds: Int = OTP_VALIDITY_SECONDS,
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null
) : UiState
