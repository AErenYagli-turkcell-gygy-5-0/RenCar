package com.turkcell.rencar.presentation.screen.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : MviViewModel<OtpState, OtpIntent, OtpEffect>(
    OtpState(
        phoneNumber = savedStateHandle.get<String>(RenCarDestination.ARG_PHONE_NUMBER)
            ?.takeIf { it.isNotBlank() }
            ?: OtpState().phoneNumber
    )
) {

    private var countdownJob: Job? = null

    init {
        startCountdown()
    }

    override fun onIntent(intent: OtpIntent) {
        when (intent) {
            OtpIntent.BackClicked,
            OtpIntent.ChangeNumberClicked -> sendEffect { OtpEffect.NavigateBack }
            is OtpIntent.DigitsChanged -> handleDigitsChanged(intent.value)
            OtpIntent.VerifyClicked -> handleVerifyClicked()
            OtpIntent.ResendClicked -> handleResendClicked()
        }
    }

    private fun handleDigitsChanged(value: String) {
        val digits = value.filter(Char::isDigit).take(OTP_LENGTH)
        setState { copy(digits = digits, errorMessage = null) }
    }

    private fun handleVerifyClicked() {
        if (state.value.isVerifying) return

        val digits = state.value.digits
        if (digits.length != OTP_LENGTH) {
            setState { copy(errorMessage = INVALID_CODE_MESSAGE) }
            return
        }

        setState { copy(isVerifying = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.verifyOtp(state.value.phoneNumber, digits)) {
                is AuthResult.Success -> {
                    setState { copy(isVerifying = false) }
                    when (result.data.user.role) {
                        ROLE_PENDING ->
                            sendEffect { OtpEffect.NavigateToLicenseVerification }

                        ROLE_CUSTOMER ->
                            sendEffect { OtpEffect.NavigateToHome }

                        else ->
                            setState { copy(errorMessage = UNSUPPORTED_ROLE_MESSAGE) }
                    }
                }

                is AuthResult.Failure -> {
                    setState { copy(isVerifying = false, errorMessage = result.error.toMessage()) }
                }
            }
        }
    }

    private fun handleResendClicked() {
        if (state.value.isResending || state.value.remainingSeconds > 0) return

        setState { copy(isResending = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.requestLogin(state.value.phoneNumber)) {
                is AuthResult.Success -> {
                    setState { copy(isResending = false, digits = "") }
                    startCountdown()
                }

                is AuthResult.Failure -> {
                    setState { copy(isResending = false, errorMessage = result.error.toMessage()) }
                }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        setState { copy(remainingSeconds = OTP_VALIDITY_SECONDS) }
        countdownJob = viewModelScope.launch {
            while (state.value.remainingSeconds > 0) {
                delay(COUNTDOWN_TICK_MILLIS)
                setState { copy(remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun AuthError.toMessage(): String = when (this) {
        AuthError.InvalidOtp -> INVALID_CODE_MESSAGE
        AuthError.UserNotFound -> USER_NOT_FOUND_MESSAGE
        AuthError.Network -> NETWORK_ERROR_MESSAGE
        AuthError.EmailAlreadyRegistered,
        AuthError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val ROLE_PENDING = "PENDING"
        const val ROLE_CUSTOMER = "CUSTOMER"
        const val UNSUPPORTED_ROLE_MESSAGE =
            "Bu hesap müşteri uygulaması için uygun değil."
        const val COUNTDOWN_TICK_MILLIS = 1_000L
        const val INVALID_CODE_MESSAGE = "Kod geçersiz veya süresi dolmuş."
        const val USER_NOT_FOUND_MESSAGE = "Bu telefon numarasına kayıtlı kullanıcı bulunamadı."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
