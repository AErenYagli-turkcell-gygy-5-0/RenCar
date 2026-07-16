package com.turkcell.rencar.presentation.screen.history

import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface HistoryIntent : UiIntent {
    data object ScreenStarted : HistoryIntent
    data object RetryClicked : HistoryIntent
    data class NavItemSelected(val item: BottomNavItem) : HistoryIntent
}
