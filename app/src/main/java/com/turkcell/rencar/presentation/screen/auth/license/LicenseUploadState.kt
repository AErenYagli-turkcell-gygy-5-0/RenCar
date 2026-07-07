package com.turkcell.rencar.presentation.screen.auth.license

import android.net.Uri
import com.turkcell.rencar.presentation.core.mvi.UiState

data class LicenseUploadState(
    val currentStep: LicenseVerificationStep = LicenseVerificationStep.LOADING,
    val frontImageUri: Uri? = null,
    val backImageUri: Uri? = null,
    val selfiePreview: ByteArray? = null,
    val isCheckingStatus: Boolean = false,
    val isUploading: Boolean = false,
    val rejectReason: String? = null,
    val errorMessage: String? = null
) : UiState

enum class LicenseVerificationStep {
    LOADING,
    LICENSE,
    SELFIE,
    APPROVAL
}
