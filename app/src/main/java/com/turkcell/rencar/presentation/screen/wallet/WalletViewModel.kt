package com.turkcell.rencar.presentation.screen.wallet

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.cards.CardError
import com.turkcell.rencar.domain.cards.CardRepository
import com.turkcell.rencar.domain.cards.CardResult
import com.turkcell.rencar.domain.wallet.WalletError
import com.turkcell.rencar.domain.wallet.WalletRepository
import com.turkcell.rencar.domain.wallet.WalletResult
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val cardRepository: CardRepository
) : MviViewModel<WalletState, WalletIntent, WalletEffect>(WalletState()) {

    override fun onIntent(intent: WalletIntent) {
        when (intent) {
            WalletIntent.ScreenStarted -> start()
            is WalletIntent.NavItemSelected -> when (intent.item) {
                BottomNavItem.Map -> sendEffect { WalletEffect.NavigateToMap }
                BottomNavItem.History -> sendEffect { WalletEffect.NavigateToHistory }
                BottomNavItem.Profile -> sendEffect { WalletEffect.NavigateToProfile }
                BottomNavItem.Wallet -> Unit
            }

            WalletIntent.TopUpClicked -> setState {
                copy(showTopUpDialog = true, topUpAmountInput = "", topUpErrorMessage = null)
            }

            is WalletIntent.TopUpAmountChanged -> setState { copy(topUpAmountInput = intent.value) }
            WalletIntent.TopUpConfirmClicked -> handleTopUpConfirmClicked()
            WalletIntent.TopUpDismissed -> setState { copy(showTopUpDialog = false) }
            WalletIntent.AddCardClicked -> setState {
                copy(
                    showAddCardDialog = true,
                    addCardBrand = CardBrand.VISA,
                    addCardLast4Input = "",
                    addCardExpMonthInput = "",
                    addCardExpYearInput = "",
                    addCardErrorMessage = null
                )
            }

            is WalletIntent.AddCardBrandChanged -> setState { copy(addCardBrand = intent.brand) }
            is WalletIntent.AddCardLast4Changed -> setState { copy(addCardLast4Input = intent.value) }
            is WalletIntent.AddCardExpMonthChanged -> setState { copy(addCardExpMonthInput = intent.value) }
            is WalletIntent.AddCardExpYearChanged -> setState { copy(addCardExpYearInput = intent.value) }
            WalletIntent.AddCardConfirmClicked -> handleAddCardConfirmClicked()
            WalletIntent.AddCardDismissed -> setState { copy(showAddCardDialog = false) }
            is WalletIntent.SetDefaultCardClicked -> handleSetDefaultCardClicked(intent.cardId)
            is WalletIntent.DeleteCardClicked -> setState {
                copy(pendingDeleteCardId = intent.cardId, deleteCardErrorMessage = null)
            }

            WalletIntent.DeleteCardConfirmed -> handleDeleteCardConfirmed()
            WalletIntent.DeleteCardDismissed -> setState {
                copy(pendingDeleteCardId = null, deleteCardErrorMessage = null)
            }
        }
    }

    private fun start() {
        setState { copy(isLoading = true, errorMessage = null) }
        loadWallet()
        loadCards()
    }

    private fun loadWallet() {
        viewModelScope.launch {
            when (val result = walletRepository.getWallet()) {
                is WalletResult.Success -> setState {
                    copy(
                        isLoading = false,
                        balance = result.data.balance,
                        transactions = result.data.transactions
                    )
                }

                is WalletResult.Failure -> setState {
                    copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun loadCards() {
        viewModelScope.launch {
            when (val result = cardRepository.getCards()) {
                is CardResult.Success -> setState { copy(cards = result.data) }
                is CardResult.Failure -> Unit
            }
        }
    }

    private fun handleTopUpConfirmClicked() {
        val amount = state.value.topUpAmountInput.replace(',', '.').toDoubleOrNull()
        if (amount == null || amount < TOPUP_MIN_AMOUNT || amount > TOPUP_MAX_AMOUNT) {
            setState { copy(topUpErrorMessage = TOPUP_RANGE_MESSAGE) }
            return
        }

        setState { copy(isTopUpSubmitting = true, topUpErrorMessage = null) }
        viewModelScope.launch {
            when (val result = walletRepository.topUp(amount)) {
                is WalletResult.Success -> setState {
                    copy(
                        isTopUpSubmitting = false,
                        showTopUpDialog = false,
                        balance = result.data.balance,
                        transactions = result.data.transactions
                    )
                }

                is WalletResult.Failure -> setState {
                    copy(isTopUpSubmitting = false, topUpErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun handleAddCardConfirmClicked() {
        val currentState = state.value
        val last4 = currentState.addCardLast4Input
        val expMonth = currentState.addCardExpMonthInput.toIntOrNull()
        val expYear = currentState.addCardExpYearInput.toIntOrNull()

        if (last4.length != CARD_LAST4_LENGTH || last4.any { !it.isDigit() }) {
            setState { copy(addCardErrorMessage = CARD_LAST4_MESSAGE) }
            return
        }
        if (expMonth == null || expMonth !in CARD_MIN_MONTH..CARD_MAX_MONTH) {
            setState { copy(addCardErrorMessage = CARD_EXP_MONTH_MESSAGE) }
            return
        }
        if (expYear == null || expYear < CARD_MIN_YEAR) {
            setState { copy(addCardErrorMessage = CARD_EXP_YEAR_MESSAGE) }
            return
        }

        setState { copy(isAddCardSubmitting = true, addCardErrorMessage = null) }
        viewModelScope.launch {
            when (
                val result = cardRepository.addCard(
                    brand = currentState.addCardBrand,
                    last4 = last4,
                    expMonth = expMonth,
                    expYear = expYear
                )
            ) {
                is CardResult.Success -> {
                    setState { copy(isAddCardSubmitting = false, showAddCardDialog = false) }
                    loadCards()
                }

                is CardResult.Failure -> setState {
                    copy(isAddCardSubmitting = false, addCardErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun handleSetDefaultCardClicked(cardId: String) {
        viewModelScope.launch {
            when (val result = cardRepository.setDefaultCard(cardId)) {
                is CardResult.Success -> loadCards()
                is CardResult.Failure -> setState { copy(errorMessage = result.error.toMessage()) }
            }
        }
    }

    private fun handleDeleteCardConfirmed() {
        val cardId = state.value.pendingDeleteCardId ?: return
        setState { copy(isDeletingCard = true, deleteCardErrorMessage = null) }
        viewModelScope.launch {
            when (val result = cardRepository.deleteCard(cardId)) {
                is CardResult.Success -> {
                    setState { copy(isDeletingCard = false, pendingDeleteCardId = null) }
                    loadCards()
                }

                is CardResult.Failure -> setState {
                    copy(isDeletingCard = false, deleteCardErrorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun WalletError.toMessage(): String = when (this) {
        WalletError.InvalidRequest -> INVALID_REQUEST_MESSAGE
        WalletError.Unauthorized -> UNAUTHORIZED_MESSAGE
        WalletError.Forbidden -> FORBIDDEN_MESSAGE
        WalletError.NotFound -> NOT_FOUND_MESSAGE
        WalletError.Conflict -> CONFLICT_MESSAGE
        WalletError.Network -> NETWORK_ERROR_MESSAGE
        WalletError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private fun CardError.toMessage(): String = when (this) {
        CardError.InvalidRequest -> INVALID_REQUEST_MESSAGE
        CardError.Unauthorized -> UNAUTHORIZED_MESSAGE
        CardError.Forbidden -> FORBIDDEN_MESSAGE
        CardError.NotFound -> NOT_FOUND_MESSAGE
        CardError.Conflict -> CONFLICT_MESSAGE
        CardError.Network -> NETWORK_ERROR_MESSAGE
        CardError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val TOPUP_MIN_AMOUNT = 10.0
        const val TOPUP_MAX_AMOUNT = 5000.0
        const val CARD_LAST4_LENGTH = 4
        const val CARD_MIN_MONTH = 1
        const val CARD_MAX_MONTH = 12
        const val CARD_MIN_YEAR = 2000
        const val TOPUP_RANGE_MESSAGE = "Tutar 10 ile 5000 TL arasında olmalı."
        const val CARD_LAST4_MESSAGE = "Kartın son 4 hanesini girin."
        const val CARD_EXP_MONTH_MESSAGE = "Son kullanma ayı 1-12 arasında olmalı."
        const val CARD_EXP_YEAR_MESSAGE = "Son kullanma yılını girin."
        const val INVALID_REQUEST_MESSAGE = "İşlem tamamlanamadı. Lütfen tekrar deneyin."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val FORBIDDEN_MESSAGE = "Bu işlem için yetkiniz yok."
        const val NOT_FOUND_MESSAGE = "Kayıt bulunamadı."
        const val CONFLICT_MESSAGE = "Bu işlem şu anda yapılamıyor."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
