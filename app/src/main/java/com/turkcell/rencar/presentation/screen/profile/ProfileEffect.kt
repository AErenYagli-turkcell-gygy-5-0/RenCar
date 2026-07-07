package com.turkcell.rencar.presentation.screen.profile

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface ProfileEffect : UiEffect {
    data object NavigateToLogin : ProfileEffect
}
