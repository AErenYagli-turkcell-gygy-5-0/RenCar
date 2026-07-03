package com.turkcell.rencar.presentation.screen.auth.otp

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface OtpIntent : UiIntent {
    data object BackClicked : OtpIntent
    data object ChangeNumberClicked : OtpIntent
    data object VerifyClicked : OtpIntent
}
