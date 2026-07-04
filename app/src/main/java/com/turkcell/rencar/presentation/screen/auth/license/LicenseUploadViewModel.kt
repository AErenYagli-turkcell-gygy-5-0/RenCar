package com.turkcell.rencar.presentation.screen.auth.license

import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LicenseUploadViewModel @Inject constructor() :
    MviViewModel<LicenseUploadState, LicenseUploadIntent, LicenseUploadEffect>(LicenseUploadState()) {

    override fun onIntent(intent: LicenseUploadIntent) {
        when (intent) {
            LicenseUploadIntent.BackClicked -> sendEffect { LicenseUploadEffect.NavigateBack }
            LicenseUploadIntent.FrontUploadClicked -> setState { copy(isFrontUploaded = true) }
            LicenseUploadIntent.BackUploadClicked -> setState { copy(isBackUploaded = true) }
            LicenseUploadIntent.ContinueClicked -> sendEffect { LicenseUploadEffect.UploadCompleted }
        }
    }
}
