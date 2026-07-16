package com.turkcell.rencar.presentation.screen.wallet

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface WalletEffect : UiEffect {
    data object NavigateToMap : WalletEffect
    data object NavigateToHistory : WalletEffect
    data object NavigateToProfile : WalletEffect
}
