package com.turkcell.rencar.presentation.screen.auth.license

import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface LicenseUploadIntent : UiIntent {
    data object BackClicked : LicenseUploadIntent
    data object FrontUploadClicked : LicenseUploadIntent
    data object BackUploadClicked : LicenseUploadIntent
    data object ContinueClicked : LicenseUploadIntent
}
