package com.turkcell.rencar.presentation.screen.auth.login

import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() :
    MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.BackClicked -> sendEffect { LoginEffect.NavigateBack }
            LoginIntent.SendCodeClicked ->
                sendEffect { LoginEffect.NavigateToOtp(state.value.phoneNumber) }
        }
    }
}
