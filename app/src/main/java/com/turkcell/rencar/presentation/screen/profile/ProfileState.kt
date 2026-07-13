package com.turkcell.rencar.presentation.screen.profile

import com.turkcell.rencar.domain.license.LicenseReviewStatus
import com.turkcell.rencar.presentation.core.mvi.UiState

data class ProfileState(
    val fullName: String = "",
    val phone: String = "",
    val profilePhoto: ByteArray? = null,
    val licenseStatus: LicenseReviewStatus? = null,
    val licenseFrontImageUrl: String? = null,
    val licenseBackImageUrl: String? = null,
    val isLicensePreviewVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
) : UiState
