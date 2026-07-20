package com.turkcell.rencar.presentation.screen.referral

import com.turkcell.rencar.domain.wallet.WalletTransaction
import com.turkcell.rencar.presentation.core.mvi.UiState

data class ReferralState(
    val referralCode: String? = null,
    val earnedTransactions: List<WalletTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
) : UiState
