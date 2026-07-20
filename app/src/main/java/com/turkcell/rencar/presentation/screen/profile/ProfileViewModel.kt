package com.turkcell.rencar.presentation.screen.profile

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.license.LicenseError
import com.turkcell.rencar.domain.license.LicenseRepository
import com.turkcell.rencar.domain.license.LicenseResult
import com.turkcell.rencar.domain.license.LicenseReviewStatus
import com.turkcell.rencar.domain.profile.ProfilePhotoRepository
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val licenseRepository: LicenseRepository,
    private val profilePhotoRepository: ProfilePhotoRepository
) : MviViewModel<ProfileState, ProfileIntent, ProfileEffect>(ProfileState()) {

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.ScreenStarted -> {
                if (!state.value.hasLoaded) loadProfile()
            }

            ProfileIntent.RetryClicked -> loadProfile()
            ProfileIntent.LicenseStatusClicked -> showLicensePreview()
            ProfileIntent.LicensePreviewDismissed ->
                setState { copy(isLicensePreviewVisible = false) }

            ProfileIntent.LogoutClicked ->
                setState { copy(showLogoutConfirmation = true, errorMessage = null) }

            ProfileIntent.LogoutConfirmationDismissed ->
                setState { copy(showLogoutConfirmation = false) }

            ProfileIntent.LogoutConfirmed -> logout()
            ProfileIntent.InviteClicked -> sendEffect { ProfileEffect.NavigateToReferral }
        }
    }

    private fun loadProfile() {
        if (state.value.isLoading) return

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val userRequest = async { authRepository.getCurrentUser() }
            val licenseRequest = async { licenseRepository.getLicenseStatus() }
            val userResult = userRequest.await()
            val licenseResult = licenseRequest.await()

            if (userResult is AuthResult.Failure && userResult.error == AuthError.Unauthorized) {
                setState { copy(isLoading = false, hasLoaded = true) }
                sendEffect { ProfileEffect.NavigateToLogin }
                return@launch
            }

            if (licenseResult is LicenseResult.Failure && licenseResult.error == LicenseError.Unauthorized) {
                setState { copy(isLoading = false, hasLoaded = true) }
                sendEffect { ProfileEffect.NavigateToLogin }
                return@launch
            }

            val user = (userResult as? AuthResult.Success)?.data
            val license = (licenseResult as? LicenseResult.Success)?.data

            if (user == null) {
                setState {
                    copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = (userResult as AuthResult.Failure).error.toMessage()
                    )
                }
                return@launch
            }

            val profilePhoto = runCatching {
                profilePhotoRepository.getProfilePhoto(user.id)
            }.getOrNull()

            setState {
                copy(
                    fullName = user.fullName,
                    phone = user.phone,
                    profilePhoto = profilePhoto,
                    licenseStatus = license?.reviewStatus,
                    licenseFrontImageUrl = license?.frontImageUrl,
                    licenseBackImageUrl = license?.backImageUrl,
                    isLoading = false,
                    hasLoaded = true,
                    errorMessage = (licenseResult as? LicenseResult.Failure)?.error?.toMessage()
                )
            }
        }
    }

    private fun showLicensePreview() {
        val licenseStatus = state.value.licenseStatus
        val canShowPreview = licenseStatus != null && licenseStatus != LicenseReviewStatus.NOT_SUBMITTED
        if (canShowPreview) {
            setState { copy(isLicensePreviewVisible = true) }
        }
    }

    private fun logout() {
        if (state.value.isLoggingOut) return

        setState { copy(isLoggingOut = true, showLogoutConfirmation = false, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.logout()) {
                is AuthResult.Success -> {
                    setState { copy(isLoggingOut = false) }
                    sendEffect { ProfileEffect.NavigateToLogin }
                }

                is AuthResult.Failure ->
                    setState {
                        copy(
                            isLoggingOut = false,
                            errorMessage = result.error.toMessage()
                        )
                    }
            }
        }
    }

    private fun AuthError.toMessage(): String = when (this) {
        AuthError.EmailAlreadyRegistered,
        AuthError.UserNotFound,
        AuthError.InvalidOtp,
        AuthError.InvalidReferralCode,
        AuthError.Unexpected -> UNEXPECTED_ERROR_MESSAGE

        AuthError.Unauthorized -> UNAUTHORIZED_MESSAGE
        AuthError.Network -> NETWORK_ERROR_MESSAGE
    }

    private fun LicenseError.toMessage(): String = when (this) {
        LicenseError.Unauthorized -> UNAUTHORIZED_MESSAGE
        LicenseError.Network -> NETWORK_ERROR_MESSAGE
        LicenseError.InvalidFile,
        LicenseError.FileTooLarge,
        LicenseError.AlreadyReviewedOrCustomer,
        LicenseError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermis. Lutfen tekrar giris yapin."
        const val NETWORK_ERROR_MESSAGE = "Internet baglantinizi kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata olustu. Lutfen tekrar deneyin."
    }
}
