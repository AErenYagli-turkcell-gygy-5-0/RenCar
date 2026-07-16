package com.turkcell.rencar.presentation.screen.wallet

import com.turkcell.rencar.domain.cards.Card
import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.wallet.WalletTransaction
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.UiState

data class WalletState(
    val balance: Double = 0.0,
    val cards: List<Card> = emptyList(),
    val transactions: List<WalletTransaction> = emptyList(),
    val showTopUpDialog: Boolean = false,
    val topUpAmountInput: String = "",
    val isTopUpSubmitting: Boolean = false,
    val topUpErrorMessage: String? = null,
    val showAddCardDialog: Boolean = false,
    val addCardBrand: CardBrand = CardBrand.VISA,
    val addCardLast4Input: String = "",
    val addCardExpMonthInput: String = "",
    val addCardExpYearInput: String = "",
    val isAddCardSubmitting: Boolean = false,
    val addCardErrorMessage: String? = null,
    val pendingDeleteCardId: String? = null,
    val isDeletingCard: Boolean = false,
    val deleteCardErrorMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedNavItem: BottomNavItem = BottomNavItem.Wallet
) : UiState
