package com.turkcell.rencar.presentation.screen.auth.license

import android.net.Uri
import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface LicenseUploadIntent : UiIntent {
    data object BackClicked : LicenseUploadIntent
    data object FrontUploadClicked : LicenseUploadIntent
    data object BackUploadClicked : LicenseUploadIntent
    data class FrontImageSelected(val uri: Uri) : LicenseUploadIntent
    data class BackImageSelected(val uri: Uri) : LicenseUploadIntent
    data object ContinueClicked : LicenseUploadIntent
}
