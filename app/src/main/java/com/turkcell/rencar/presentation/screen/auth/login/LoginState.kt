package com.turkcell.rencar.presentation.screen.auth.login

import com.turkcell.rencar.presentation.core.mvi.UiState

data class LoginState(
    val phoneNumber: String = "532 000 00 00"
) : UiState
