package com.turkcell.rencar.presentation.screen.splash

import com.turkcell.rencar.presentation.core.mvi.UiState

data class SplashState(
    val currentPage: Int = 0
) : UiState {
    companion object {
        const val PAGE_COUNT = 3
    }
}
