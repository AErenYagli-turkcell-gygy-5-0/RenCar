package com.turkcell.rencar.presentation.screen.auth.otp

import androidx.lifecycle.SavedStateHandle
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : MviViewModel<OtpState, OtpIntent, OtpEffect>(
    OtpState(
        phoneNumber = savedStateHandle.get<String>(RenCarDestination.ARG_PHONE_NUMBER)
            ?.takeIf { it.isNotBlank() }
            ?: OtpState().phoneNumber
    )
) {

    override fun onIntent(intent: OtpIntent) {
        when (intent) {
            OtpIntent.BackClicked,
            OtpIntent.ChangeNumberClicked -> sendEffect { OtpEffect.NavigateBack }
            OtpIntent.VerifyClicked -> sendEffect { OtpEffect.VerificationCompleted }
        }
    }
}
