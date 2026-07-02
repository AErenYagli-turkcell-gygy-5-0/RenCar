package com.turkcell.rencar.presentation.screen.auth.otp

import com.turkcell.rencar.presentation.core.mvi.UiEffect
import com.turkcell.rencar.presentation.core.mvi.UiIntent
import com.turkcell.rencar.presentation.core.mvi.UiState

const val OTP_LENGTH = 6

data class OtpState(
    val phoneNumber: String = "+90 532 000 00 00",
    val digits: String = "482",
    val remainingSeconds: Int = 42
) : UiState

sealed interface OtpIntent : UiIntent {
    data object BackClicked : OtpIntent
    data object ChangeNumberClicked : OtpIntent
    data object VerifyClicked : OtpIntent
}

sealed interface OtpEffect : UiEffect {
    data object NavigateBack : OtpEffect
    data object VerificationCompleted : OtpEffect
}
