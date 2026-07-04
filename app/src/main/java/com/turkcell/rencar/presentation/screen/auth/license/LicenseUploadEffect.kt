package com.turkcell.rencar.presentation.screen.auth.license

import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface LicenseUploadEffect : UiEffect {
    data object NavigateBack : LicenseUploadEffect
    data object UploadCompleted : LicenseUploadEffect
}
