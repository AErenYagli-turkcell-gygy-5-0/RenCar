package com.turkcell.rencar.presentation.screen.cardetail

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface CarDetailIntent : UiIntent {
    data object ScreenStarted : CarDetailIntent
    data object RetryClicked : CarDetailIntent
    data object BackClicked : CarDetailIntent
}
