package com.turkcell.rencar.presentation.screen.cardetail

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface CarDetailEffect : UiEffect {
    data object NavigateBack : CarDetailEffect
}
