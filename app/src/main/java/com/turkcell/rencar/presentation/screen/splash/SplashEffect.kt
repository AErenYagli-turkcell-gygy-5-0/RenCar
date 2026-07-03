package com.turkcell.rencar.presentation.screen.splash

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface SplashEffect : UiEffect {
    data object NavigateToLogin : SplashEffect
}
