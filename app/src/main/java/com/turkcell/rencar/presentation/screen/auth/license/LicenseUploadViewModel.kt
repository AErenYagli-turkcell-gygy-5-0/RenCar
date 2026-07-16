package com.turkcell.rencar.presentation.screen.auth.license

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.auth.AuthRepository
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.license.LicenseError
import com.turkcell.rencar.domain.license.LicenseRepository
import com.turkcell.rencar.domain.license.LicenseResult
import com.turkcell.rencar.domain.license.LicenseReviewStatus
import com.turkcell.rencar.domain.license.LicenseStatus
import com.turkcell.rencar.domain.profile.ProfilePhotoRepository
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicenseUploadViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository,
    private val authRepository: AuthRepository,
    private val profilePhotoRepository: ProfilePhotoRepository
) : MviViewModel<LicenseUploadState, LicenseUploadIntent, LicenseUploadEffect>(LicenseUploadState()) {

    init {
        loadLicenseStatus()
    }

    override fun onIntent(intent: LicenseUploadIntent) {
        when (intent) {
            LicenseUploadIntent.BackClicked -> handleBackClicked()
            LicenseUploadIntent.FrontUploadClicked,
            LicenseUploadIntent.BackUploadClicked -> Unit

            is LicenseUploadIntent.FrontImageSelected ->
                setState { copy(frontImageUri = intent.uri, errorMessage = null) }

            is LicenseUploadIntent.BackImageSelected ->
                setState { copy(backImageUri = intent.uri, errorMessage = null) }

            LicenseUploadIntent.SelfieCaptureClicked ->
                sendEffect { LicenseUploadEffect.OpenSelfieCamera }

            is LicenseUploadIntent.SelfieCaptured ->
                setState { copy(selfiePreview = intent.preview, errorMessage = null) }

            LicenseUploadIntent.ContinueClicked -> handleContinueClicked()
            LicenseUploadIntent.CheckStatusClicked,
            LicenseUploadIntent.RetryStatusClicked -> loadLicenseStatus()
        }
    }

    private fun handleBackClicked() {
        if (state.value.currentStep == LicenseVerificationStep.SELFIE) {
            setState { copy(currentStep = LicenseVerificationStep.LICENSE, errorMessage = null) }
        } else {
            sendEffect { LicenseUploadEffect.NavigateBack }
        }
    }

    private fun handleContinueClicked() {
        when (state.value.currentStep) {
            LicenseVerificationStep.LICENSE -> moveToSelfie()
            LicenseVerificationStep.SELFIE -> uploadLicense()
            LicenseVerificationStep.LOADING,
            LicenseVerificationStep.APPROVAL -> Unit
        }
    }

    private fun moveToSelfie() {
        if (state.value.frontImageUri == null || state.value.backImageUri == null) {
            setState { copy(errorMessage = MISSING_IMAGES_MESSAGE) }
            return
        }

        setState { copy(currentStep = LicenseVerificationStep.SELFIE, errorMessage = null) }
    }

    private fun uploadLicense() {
        if (state.value.isUploading) return

        val frontImageUri = state.value.frontImageUri ?: return
        val backImageUri = state.value.backImageUri ?: return
        val selfiePreview = state.value.selfiePreview
        if (selfiePreview == null) {
            setState { copy(errorMessage = MISSING_SELFIE_MESSAGE) }
            return
        }

        setState { copy(isUploading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = licenseRepository.uploadLicense(frontImageUri, backImageUri, selfiePreview)) {
                is LicenseResult.Success -> {
                    saveSelfieAsProfilePhoto(selfiePreview)
                    setState {
                        copy(
                            currentStep = LicenseVerificationStep.APPROVAL,
                            frontImageUri = null,
                            backImageUri = null,
                            selfiePreview = null,
                            isUploading = false,
                            rejectReason = null
                        )
                    }
                }

                is LicenseResult.Failure -> {
                    if (result.error == LicenseError.AlreadyReviewedOrCustomer) {
                        setState { copy(isUploading = false, selfiePreview = null) }
                        loadLicenseStatus()
                    } else {
                        setState {
                            copy(isUploading = false, errorMessage = result.error.toMessage())
                        }
                    }
                }
            }
        }
    }

    internal suspend fun saveSelfieAsProfilePhoto(selfiePreview: ByteArray) {
        val user = (authRepository.getCurrentUser() as? AuthResult.Success)?.data ?: return
        runCatching {
            profilePhotoRepository.saveProfilePhoto(user.id, selfiePreview)
        }
    }

    private fun loadLicenseStatus() {
        if (state.value.isCheckingStatus) return

        setState { copy(isCheckingStatus = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = licenseRepository.getLicenseStatus()) {
                is LicenseResult.Success -> handleLicenseStatus(result.data)
                is LicenseResult.Failure ->
                    setState {
                        copy(
                            currentStep = LicenseVerificationStep.LOADING,
                            isCheckingStatus = false,
                            errorMessage = result.error.toMessage()
                        )
                    }
            }
        }
    }

    private fun handleLicenseStatus(status: LicenseStatus) {
        when (status.reviewStatus) {
            LicenseReviewStatus.NOT_SUBMITTED ->
                setState {
                    copy(
                        currentStep = LicenseVerificationStep.LICENSE,
                        isCheckingStatus = false,
                        rejectReason = null
                    )
                }

            LicenseReviewStatus.REJECTED ->
                setState {
                    copy(
                        currentStep = LicenseVerificationStep.LICENSE,
                        isCheckingStatus = false,
                        rejectReason = status.rejectReason
                    )
                }

            LicenseReviewStatus.UNDER_REVIEW ->
                setState {
                    copy(
                        currentStep = LicenseVerificationStep.APPROVAL,
                        frontImageUri = null,
                        backImageUri = null,
                        selfiePreview = null,
                        isCheckingStatus = false,
                        rejectReason = null
                    )
                }

            LicenseReviewStatus.APPROVED -> refreshSessionAndNavigateHome()
        }
    }

    private fun refreshSessionAndNavigateHome() {
        viewModelScope.launch {
            when (val result = authRepository.refreshSession()) {
                is AuthResult.Success -> {
                    setState { copy(isCheckingStatus = false) }
                    if (result.data.user.role == ROLE_CUSTOMER) {
                        sendEffect { LicenseUploadEffect.NavigateHome }
                    } else {
                        sendEffect { LicenseUploadEffect.NavigateToLogin }
                    }
                }

                is AuthResult.Failure -> {
                    setState { copy(isCheckingStatus = false) }
                    sendEffect { LicenseUploadEffect.NavigateToLogin }
                }
            }
        }
    }

    private fun LicenseError.toMessage(): String = when (this) {
        LicenseError.Unauthorized -> UNAUTHORIZED_MESSAGE
        LicenseError.InvalidFile -> INVALID_FILE_MESSAGE
        LicenseError.FileTooLarge -> FILE_TOO_LARGE_MESSAGE
        LicenseError.AlreadyReviewedOrCustomer -> ALREADY_REVIEWED_MESSAGE
        LicenseError.Network -> NETWORK_ERROR_MESSAGE
        LicenseError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val ROLE_CUSTOMER = "CUSTOMER"
        const val MISSING_IMAGES_MESSAGE =
            "Devam etmeden önce ön ve arka yüz fotoğraflarını seçmelisiniz."
        const val MISSING_SELFIE_MESSAGE = "Devam etmeden önce selfie çekmelisiniz."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val INVALID_FILE_MESSAGE =
            "Dosya eksik veya desteklenmeyen bir formatta (jpg/png olmalı)."
        const val FILE_TOO_LARGE_MESSAGE = "Dosya boyutu çok büyük (maksimum 5MB)."
        const val ALREADY_REVIEWED_MESSAGE = "Ehliyetiniz zaten incelemede veya onaylı."
        const val NETWORK_ERROR_MESSAGE =
            "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
