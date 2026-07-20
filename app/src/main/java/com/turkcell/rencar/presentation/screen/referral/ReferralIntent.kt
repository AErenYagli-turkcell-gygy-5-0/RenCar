package com.turkcell.rencar.presentation.screen.referral

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface ReferralIntent : UiIntent {
    data object ScreenStarted : ReferralIntent
    data object RetryClicked : ReferralIntent
    data object BackClicked : ReferralIntent
    data object ShareClicked : ReferralIntent
    data object CopyCodeClicked : ReferralIntent
}
