package com.turkcell.rencar.presentation.screen.profile

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface ProfileIntent : UiIntent {
    data object ScreenStarted : ProfileIntent
    data object RetryClicked : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data object LogoutConfirmed : ProfileIntent
    data object LogoutConfirmationDismissed : ProfileIntent
}
