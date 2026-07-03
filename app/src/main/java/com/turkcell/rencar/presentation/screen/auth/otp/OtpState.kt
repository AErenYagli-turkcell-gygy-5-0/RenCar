package com.turkcell.rencar.presentation.screen.auth.otp

import com.turkcell.rencar.presentation.core.mvi.UiState

const val OTP_LENGTH = 6

data class OtpState(
    val phoneNumber: String = "+90 532 000 00 00",
    val digits: String = "482",
    val remainingSeconds: Int = 42
) : UiState
