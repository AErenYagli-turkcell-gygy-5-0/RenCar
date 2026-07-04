package com.turkcell.rencar.presentation.screen.auth.license

import android.net.Uri
import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface LicenseUploadIntent : UiIntent {
    data object BackClicked : LicenseUploadIntent
    data object FrontUploadClicked : LicenseUploadIntent
    data object BackUploadClicked : LicenseUploadIntent
    data class FrontImageSelected(val uri: Uri) : LicenseUploadIntent
    data class BackImageSelected(val uri: Uri) : LicenseUploadIntent
    data object SelfieCaptureClicked : LicenseUploadIntent
    data class SelfieCaptured(val preview: ByteArray) : LicenseUploadIntent
    data object ContinueClicked : LicenseUploadIntent
    data object CheckStatusClicked : LicenseUploadIntent
    data object RetryStatusClicked : LicenseUploadIntent
}
