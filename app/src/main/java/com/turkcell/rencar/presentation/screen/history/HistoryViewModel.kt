package com.turkcell.rencar.presentation.screen.history

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val rentalRepository: RentalRepository
) : MviViewModel<HistoryState, HistoryIntent, HistoryEffect>(HistoryState()) {

    override fun onIntent(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.ScreenStarted -> if (!state.value.hasLoaded) loadHistory()
            HistoryIntent.RetryClicked -> loadHistory()
            is HistoryIntent.NavItemSelected -> when (intent.item) {
                BottomNavItem.Map -> sendEffect { HistoryEffect.NavigateToMap }
                BottomNavItem.Profile -> sendEffect { HistoryEffect.NavigateToProfile }
                BottomNavItem.Wallet -> sendEffect { HistoryEffect.NavigateToWallet }
                else -> setState { copy(selectedNavItem = intent.item) }
            }
        }
    }

    private fun loadHistory() {
        if (state.value.isLoading) return

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val historyResult = rentalRepository.getRentalHistory()) {
                is RentalResult.Success -> {
                    val stats = (rentalRepository.getRentalStats() as? RentalResult.Success)?.data
                    setState {
                        copy(
                            items = historyResult.data,
                            tripCount = stats?.tripCount ?: historyResult.data.size,
                            totalSpent = stats?.totalSpent ?: 0.0,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = null
                        )
                    }
                }

                is RentalResult.Failure -> setState {
                    copy(isLoading = false, hasLoaded = true, errorMessage = historyResult.error.toMessage())
                }
            }
        }
    }

    private fun RentalError.toMessage(): String = when (this) {
        RentalError.Unauthorized -> UNAUTHORIZED_MESSAGE
        RentalError.Forbidden -> FORBIDDEN_MESSAGE
        RentalError.Network -> NETWORK_ERROR_MESSAGE
        RentalError.InvalidRequest,
        RentalError.NotFound,
        RentalError.Conflict,
        RentalError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val FORBIDDEN_MESSAGE = "Bu kiralama geçmişine erişemiyorsunuz."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Kiralama geçmişi yüklenemedi. Lütfen tekrar deneyin."
    }
}
