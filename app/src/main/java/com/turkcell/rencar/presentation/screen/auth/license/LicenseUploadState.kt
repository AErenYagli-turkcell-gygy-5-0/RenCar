package com.turkcell.rencar.presentation.screen.auth.license

import com.turkcell.rencar.presentation.core.mvi.UiState

data class LicenseUploadState(
    val isFrontUploaded: Boolean = false,
    val isBackUploaded: Boolean = false
) : UiState
