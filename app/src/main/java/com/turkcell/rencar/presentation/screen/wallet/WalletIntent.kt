package com.turkcell.rencar.presentation.screen.wallet

import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface WalletIntent : UiIntent {
    data object ScreenStarted : WalletIntent
    data class NavItemSelected(val item: BottomNavItem) : WalletIntent
    data object TopUpClicked : WalletIntent
    data class TopUpAmountChanged(val value: String) : WalletIntent
    data object TopUpConfirmClicked : WalletIntent
    data object TopUpDismissed : WalletIntent
    data object AddCardClicked : WalletIntent
    data class AddCardBrandChanged(val brand: CardBrand) : WalletIntent
    data class AddCardLast4Changed(val value: String) : WalletIntent
    data class AddCardExpMonthChanged(val value: String) : WalletIntent
    data class AddCardExpYearChanged(val value: String) : WalletIntent
    data object AddCardConfirmClicked : WalletIntent
    data object AddCardDismissed : WalletIntent
    data class SetDefaultCardClicked(val cardId: String) : WalletIntent
    data class DeleteCardClicked(val cardId: String) : WalletIntent
    data object DeleteCardConfirmed : WalletIntent
    data object DeleteCardDismissed : WalletIntent
}
