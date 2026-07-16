package com.turkcell.rencar.presentation.screen.history

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface HistoryEffect : UiEffect {
    data object NavigateToMap : HistoryEffect
    data object NavigateToProfile : HistoryEffect
}
