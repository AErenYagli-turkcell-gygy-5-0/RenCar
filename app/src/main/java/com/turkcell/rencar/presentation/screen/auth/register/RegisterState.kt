package com.turkcell.rencar.presentation.screen.auth.register

import com.turkcell.rencar.presentation.core.mvi.UiState

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
