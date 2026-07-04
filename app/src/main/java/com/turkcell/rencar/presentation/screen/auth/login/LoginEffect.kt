package com.turkcell.rencar.presentation.screen.auth.login

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface LoginEffect : UiEffect {
    data object NavigateBack : LoginEffect
    data object NavigateToRegister : LoginEffect
    data class NavigateToOtp(val phoneNumber: String) : LoginEffect
}
