package com.turkcell.rencar.presentation.screen.payment

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface PaymentEffect : UiEffect {
    data object NavigateHome : PaymentEffect
    data object NavigateToWallet : PaymentEffect
}
