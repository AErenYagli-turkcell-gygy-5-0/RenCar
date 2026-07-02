package com.turkcell.rencar.presentation.screen.splash

import com.turkcell.rencar.presentation.core.mvi.UiEffect
import com.turkcell.rencar.presentation.core.mvi.UiIntent
import com.turkcell.rencar.presentation.core.mvi.UiState

data object SplashState : UiState

sealed interface SplashIntent : UiIntent {
    data object GetStartedClicked : SplashIntent
    data object LoginClicked : SplashIntent
}

sealed interface SplashEffect : UiEffect {
    data object NavigateToLogin : SplashEffect
}
