package com.turkcell.rencar.presentation.screen.auth.register

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface RegisterIntent : UiIntent {
    data object BackClicked : RegisterIntent
    data object LoginClicked : RegisterIntent
    data class EmailChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class FullNameChanged(val value: String) : RegisterIntent
    data class PhoneNumberChanged(val value: String) : RegisterIntent
    data object CreateAccountClicked : RegisterIntent
}
