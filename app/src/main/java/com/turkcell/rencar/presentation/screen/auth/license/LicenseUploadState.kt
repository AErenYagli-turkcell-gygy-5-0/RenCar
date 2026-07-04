package com.turkcell.rencar.presentation.screen.auth.license

import android.net.Uri
import com.turkcell.rencar.presentation.core.mvi.UiState

data class LicenseUploadState(
    val frontImageUri: Uri? = null,
    val backImageUri: Uri? = null,
    val isUploading: Boolean = false,
    val errorMessage: String? = null
) : UiState
