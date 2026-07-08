package com.turkcell.rencar.presentation.screen.auth.login

import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) :
    MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.BackClicked -> sendEffect { LoginEffect.NavigateBack }
            LoginIntent.SignUpClicked -> sendEffect { LoginEffect.NavigateToRegister }
            is LoginIntent.PhoneNumberChanged -> handlePhoneNumberChanged(intent.value)
            LoginIntent.SendCodeClicked -> handleSendCode()
        }
    }

    private fun handlePhoneNumberChanged(value: String) {
        val digits = value.filter(Char::isDigit).take(PHONE_NUMBER_LENGTH)
        setState {
            copy(
                phoneNumber = digits,
                errorMessage = null
            )
        }
    }

    private fun handleSendCode() {
        if (state.value.isLoading) return

        val digits = state.value.phoneNumber.filter(Char::isDigit)
        if (digits.length != PHONE_NUMBER_LENGTH) {
            setState { copy(errorMessage = INVALID_PHONE_MESSAGE) }
            return
        }

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.requestLogin("$TURKEY_PHONE_PREFIX$digits")) {
                is AuthResult.Success -> {
                    setState { copy(isLoading = false) }
                    sendEffect { LoginEffect.NavigateToOtp(result.data.phone) }
                }

                is AuthResult.Failure -> {
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = result.error.toMessage()
                        )
                    }
                }
            }
        }
    }

    private fun AuthError.toMessage(): String = when (this) {
        AuthError.UserNotFound -> USER_NOT_FOUND_MESSAGE
        AuthError.Unauthorized -> UNAUTHORIZED_MESSAGE
        AuthError.Network -> NETWORK_ERROR_MESSAGE
        AuthError.EmailAlreadyRegistered,
        AuthError.InvalidOtp,
        AuthError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val PHONE_NUMBER_LENGTH = 10
        const val TURKEY_PHONE_PREFIX = "+90"
        const val INVALID_PHONE_MESSAGE = "Telefon numarası 10 haneli olmalıdır."
        const val USER_NOT_FOUND_MESSAGE = "Bu telefon numarasına kayıtlı kullanıcı bulunamadı."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
