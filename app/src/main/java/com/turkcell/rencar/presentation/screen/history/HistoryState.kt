package com.turkcell.rencar.presentation.screen.history

import com.turkcell.rencar.domain.rental.RentalHistoryItem
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.UiState

data class HistoryState(
    val items: List<RentalHistoryItem> = emptyList(),
    val tripCount: Int = 0,
    val totalSpent: Double = 0.0,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
    val selectedNavItem: BottomNavItem = BottomNavItem.History
) : UiState
