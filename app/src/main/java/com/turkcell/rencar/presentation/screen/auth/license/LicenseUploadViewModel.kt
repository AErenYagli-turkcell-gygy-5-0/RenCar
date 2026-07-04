package com.turkcell.rencar.presentation.screen.auth.license

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.license.LicenseError
import com.turkcell.rencar.domain.license.LicenseRepository
import com.turkcell.rencar.domain.license.LicenseResult
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicenseUploadViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository
) : MviViewModel<LicenseUploadState, LicenseUploadIntent, LicenseUploadEffect>(LicenseUploadState()) {

    override fun onIntent(intent: LicenseUploadIntent) {
        when (intent) {
            LicenseUploadIntent.BackClicked -> sendEffect { LicenseUploadEffect.NavigateBack }

            // Galeri seçici Route katmanında (rememberLauncherForActivityResult) yakalanır;
            // buraya ulaşırsa yapılacak bir şey yoktur.
            LicenseUploadIntent.FrontUploadClicked,
            LicenseUploadIntent.BackUploadClicked -> Unit

            is LicenseUploadIntent.FrontImageSelected ->
                setState { copy(frontImageUri = intent.uri, errorMessage = null) }

            is LicenseUploadIntent.BackImageSelected ->
                setState { copy(backImageUri = intent.uri, errorMessage = null) }

            LicenseUploadIntent.ContinueClicked -> handleContinueClicked()
        }
    }

    private fun handleContinueClicked() {
        if (state.value.isUploading) return

        val frontImageUri = state.value.frontImageUri
        val backImageUri = state.value.backImageUri
        if (frontImageUri == null || backImageUri == null) {
            setState { copy(errorMessage = MISSING_IMAGES_MESSAGE) }
            return
        }

        setState { copy(isUploading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = licenseRepository.uploadLicense(frontImageUri, backImageUri)) {
                is LicenseResult.Success -> {
                    setState { copy(isUploading = false) }
                    sendEffect { LicenseUploadEffect.UploadCompleted }
                }

                is LicenseResult.Failure -> {
                    setState { copy(isUploading = false, errorMessage = result.error.toMessage()) }
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
        const val MISSING_IMAGES_MESSAGE = "Devam etmeden önce ön ve arka yüz fotoğraflarını seçmelisiniz."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val INVALID_FILE_MESSAGE = "Dosya eksik veya desteklenmeyen bir formatta (jpg/png olmalı)."
        const val FILE_TOO_LARGE_MESSAGE = "Dosya boyutu çok büyük (maksimum 5MB)."
        const val ALREADY_REVIEWED_MESSAGE = "Ehliyetiniz zaten incelemede veya onaylı."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
