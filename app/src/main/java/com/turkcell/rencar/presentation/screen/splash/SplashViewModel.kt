package com.turkcell.rencar.presentation.screen.splash

import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() :
    MviViewModel<SplashState, SplashIntent, SplashEffect>(SplashState) {

    override fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.GetStartedClicked,
            SplashIntent.LoginClicked -> sendEffect { SplashEffect.NavigateToLogin }
        }
    }
}
