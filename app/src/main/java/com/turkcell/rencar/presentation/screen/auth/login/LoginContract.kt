package com.turkcell.rencar.presentation.screen.auth.login

import com.turkcell.rencar.presentation.core.mvi.UiEffect
import com.turkcell.rencar.presentation.core.mvi.UiIntent
import com.turkcell.rencar.presentation.core.mvi.UiState

data class LoginState(
    val phoneNumber: String = "532 000 00 00"
) : UiState

sealed interface LoginIntent : UiIntent {
    data object BackClicked : LoginIntent
    data object SendCodeClicked : LoginIntent
}

sealed interface LoginEffect : UiEffect {
    data object NavigateBack : LoginEffect
    data class NavigateToOtp(val phoneNumber: String) : LoginEffect
}
