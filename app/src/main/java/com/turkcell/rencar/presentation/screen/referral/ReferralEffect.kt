package com.turkcell.rencar.presentation.screen.referral

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface ReferralEffect : UiEffect {
    data object NavigateBack : ReferralEffect
    data class ShareReferralCode(val code: String) : ReferralEffect
    data class CopyReferralCode(val code: String) : ReferralEffect
}
