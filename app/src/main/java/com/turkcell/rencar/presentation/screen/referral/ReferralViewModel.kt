package com.turkcell.rencar.presentation.screen.referral

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.wallet.WalletError
import com.turkcell.rencar.domain.wallet.WalletRepository
import com.turkcell.rencar.domain.wallet.WalletResult
import com.turkcell.rencar.domain.wallet.WalletTransactionType
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository
) : MviViewModel<ReferralState, ReferralIntent, ReferralEffect>(ReferralState()) {

    override fun onIntent(intent: ReferralIntent) {
        when (intent) {
            ReferralIntent.ScreenStarted -> {
                if (!state.value.hasLoaded) loadReferralInfo()
            }

            ReferralIntent.RetryClicked -> loadReferralInfo()
            ReferralIntent.BackClicked -> sendEffect { ReferralEffect.NavigateBack }

            ReferralIntent.ShareClicked -> state.value.referralCode?.let { code ->
                sendEffect { ReferralEffect.ShareReferralCode(code) }
            }

            ReferralIntent.CopyCodeClicked -> state.value.referralCode?.let { code ->
                sendEffect { ReferralEffect.CopyReferralCode(code) }
            }
        }
    }

    private fun loadReferralInfo() {
        if (state.value.isLoading) return

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val userRequest = async { authRepository.getCurrentUser() }
            val walletRequest = async { walletRepository.getWallet() }
            val userResult = userRequest.await()
            val walletResult = walletRequest.await()

            val user = (userResult as? AuthResult.Success)?.data
            if (user == null) {
                setState {
                    copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = (userResult as AuthResult.Failure).error.toMessage()
                    )
                }
                return@launch
            }

            val earnedTransactions = (walletResult as? WalletResult.Success)?.data?.transactions
                ?.filter { it.type == WalletTransactionType.REFERRAL_BONUS }
                .orEmpty()

            setState {
                copy(
                    referralCode = user.referralCode,
                    earnedTransactions = earnedTransactions,
                    isLoading = false,
                    hasLoaded = true,
                    errorMessage = (walletResult as? WalletResult.Failure)?.error?.toMessage()
                )
            }
        }
    }

    private fun AuthError.toMessage(): String = when (this) {
        AuthError.Unauthorized -> UNAUTHORIZED_MESSAGE
        AuthError.Network -> NETWORK_ERROR_MESSAGE
        AuthError.EmailAlreadyRegistered,
        AuthError.UserNotFound,
        AuthError.InvalidOtp,
        AuthError.InvalidReferralCode,
        AuthError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private fun WalletError.toMessage(): String = when (this) {
        WalletError.Unauthorized -> UNAUTHORIZED_MESSAGE
        WalletError.Network -> NETWORK_ERROR_MESSAGE
        WalletError.InvalidRequest,
        WalletError.Forbidden,
        WalletError.NotFound,
        WalletError.Conflict,
        WalletError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
