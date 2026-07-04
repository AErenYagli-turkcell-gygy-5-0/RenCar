package com.turkcell.rencar.presentation.screen.splash

import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() :
    MviViewModel<SplashState, SplashIntent, SplashEffect>(SplashState()) {

    override fun onIntent(intent: SplashIntent) {
        when (intent) {
            is SplashIntent.PageChanged -> {
                if (intent.page in 0 until SplashState.PAGE_COUNT) {
                    setState { copy(currentPage = intent.page) }
                }
            }
            SplashIntent.GetStartedClicked,
            SplashIntent.LoginClicked -> sendEffect { SplashEffect.NavigateToLogin }
        }
    }
}
