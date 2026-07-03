package com.turkcell.rencar.presentation.screen.auth.login

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface LoginIntent : UiIntent {
    data object BackClicked : LoginIntent
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data object SendCodeClicked : LoginIntent
}
