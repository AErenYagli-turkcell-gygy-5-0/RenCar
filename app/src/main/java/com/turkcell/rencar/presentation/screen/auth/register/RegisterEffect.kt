package com.turkcell.rencar.presentation.screen.auth.register

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface RegisterEffect : UiEffect {
    data object NavigateBack : RegisterEffect
    data class NavigateToOtp(val phoneNumber: String) : RegisterEffect
}
