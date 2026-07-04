package com.turkcell.rencar.presentation.screen.splash

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface SplashIntent : UiIntent {
    data class PageChanged(val page: Int) : SplashIntent
    data object GetStartedClicked : SplashIntent
    data object LoginClicked : SplashIntent
}
