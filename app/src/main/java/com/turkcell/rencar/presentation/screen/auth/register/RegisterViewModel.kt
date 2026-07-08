package com.turkcell.rencar.presentation.screen.auth.register

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MviViewModel<RegisterState, RegisterIntent, RegisterEffect>(RegisterState()) {

    override fun onIntent(intent: RegisterIntent) {
        when (intent) {
            RegisterIntent.BackClicked,
            RegisterIntent.LoginClicked -> sendEffect { RegisterEffect.NavigateBack }
            is RegisterIntent.EmailChanged -> setState {
                copy(email = intent.value.trim(), errorMessage = null)
            }
            is RegisterIntent.PasswordChanged -> setState {
                copy(password = intent.value, errorMessage = null)
            }
            is RegisterIntent.FullNameChanged -> setState {
                copy(fullName = intent.value, errorMessage = null)
            }
            is RegisterIntent.PhoneNumberChanged -> handlePhoneNumberChanged(intent.value)
            RegisterIntent.CreateAccountClicked -> handleCreateAccount()
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

    private fun handleCreateAccount() {
        if (state.value.isLoading) return

        val currentState = state.value
        val phone = currentState.phoneNumber.filter(Char::isDigit)
        val validationMessage = validate(
            email = currentState.email,
            password = currentState.password,
            fullName = currentState.fullName,
            phone = phone
        )
        if (validationMessage != null) {
            setState { copy(errorMessage = validationMessage) }
            return
        }

        val normalizedPhone = "$TURKEY_PHONE_PREFIX$phone"
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val registerResult = authRepository.register(
                RegisterRequest(
                    email = currentState.email.trim(),
                    password = currentState.password,
                    fullName = currentState.fullName.trim(),
                    phone = normalizedPhone
                )
            )

            when (registerResult) {
                is AuthResult.Success -> requestOtp(registerResult.data.phone)
                is AuthResult.Failure -> setState {
                    copy(isLoading = false, errorMessage = registerResult.error.toMessage())
                }
            }
        }
    }

    private suspend fun requestOtp(phone: String) {
        when (val loginResult = authRepository.requestLogin(phone)) {
            is AuthResult.Success -> {
                setState { copy(isLoading = false) }
                sendEffect { RegisterEffect.NavigateToOtp(loginResult.data.phone) }
            }
            is AuthResult.Failure -> setState {
                copy(isLoading = false, errorMessage = loginResult.error.toMessage())
            }
        }
    }

    private fun validate(
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): String? = when {
        fullName.trim().isBlank() -> INVALID_FULL_NAME_MESSAGE
        !email.trim().isValidEmail() -> INVALID_EMAIL_MESSAGE
        password.length < MIN_PASSWORD_LENGTH -> INVALID_PASSWORD_MESSAGE
        phone.length != PHONE_NUMBER_LENGTH -> INVALID_PHONE_MESSAGE
        else -> null
    }

    private fun String.isValidEmail(): Boolean =
        contains("@") && substringAfter("@").contains(".")

    private fun AuthError.toMessage(): String = when (this) {
        AuthError.EmailAlreadyRegistered -> EMAIL_ALREADY_REGISTERED_MESSAGE
        AuthError.Unauthorized -> UNAUTHORIZED_MESSAGE
        AuthError.Network -> NETWORK_ERROR_MESSAGE
        AuthError.UserNotFound,
        AuthError.InvalidOtp,
        AuthError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val PHONE_NUMBER_LENGTH = 10
        const val MIN_PASSWORD_LENGTH = 6
        const val TURKEY_PHONE_PREFIX = "+90"
        const val INVALID_FULL_NAME_MESSAGE = "Ad soyad alanı boş bırakılamaz."
        const val INVALID_EMAIL_MESSAGE = "Geçerli bir e-posta adresi girin."
        const val INVALID_PASSWORD_MESSAGE = "Şifre en az 6 karakter olmalıdır."
        const val INVALID_PHONE_MESSAGE = "Telefon numarası 10 haneli olmalıdır."
        const val EMAIL_ALREADY_REGISTERED_MESSAGE = "Bu e-posta adresi zaten kayıtlı."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
