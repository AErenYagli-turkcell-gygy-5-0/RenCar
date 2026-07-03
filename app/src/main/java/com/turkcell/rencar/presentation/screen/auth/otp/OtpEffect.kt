package com.turkcell.rencar.presentation.screen.auth.otp

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface OtpEffect : UiEffect {
    data object NavigateBack : OtpEffect
    data object VerificationCompleted : OtpEffect
}
