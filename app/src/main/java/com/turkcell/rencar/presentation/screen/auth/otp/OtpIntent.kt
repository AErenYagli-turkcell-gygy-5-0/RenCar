package com.turkcell.rencar.presentation.screen.auth.otp

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface OtpIntent : UiIntent {
    data object BackClicked : OtpIntent
    data object ChangeNumberClicked : OtpIntent
    data class DigitsChanged(val value: String) : OtpIntent
    data object VerifyClicked : OtpIntent
    data object ResendClicked : OtpIntent
}
