package com.turkcell.rencar.presentation.screen.rental.photo

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalPhotoSide
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RentalPhotoUploadViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val vehicleRepository: VehicleRepository
) : MviViewModel<RentalPhotoUploadState, RentalPhotoUploadIntent, RentalPhotoUploadEffect>(
    RentalPhotoUploadState()
) {

    override fun onIntent(intent: RentalPhotoUploadIntent) {
        when (intent) {
            is RentalPhotoUploadIntent.ScreenStarted ->
                start(intent.rentalId, intent.vehicleId, intent.mode)

            RentalPhotoUploadIntent.BackClicked -> handleBackClicked()
            is RentalPhotoUploadIntent.PhotoSelected -> handlePhotoSelected(intent.side, intent.uri)
            RentalPhotoUploadIntent.PrimaryActionClicked -> handlePrimaryActionClicked()
        }
    }

    private fun start(rentalId: String, vehicleId: String, mode: RentalPhotoUploadMode) {
        if (state.value.rentalId == rentalId && state.value.mode == mode) return

        setState {
            copy(
                rentalId = rentalId,
                vehicleId = vehicleId,
                mode = mode,
                isLoading = true,
                errorMessage = null
            )
        }
        loadVehicle(vehicleId)
        if (mode == RentalPhotoUploadMode.START_TRIP) {
            loadExistingPhotos(rentalId)
        } else {
            setState { copy(isLoading = false) }
        }
    }

    private fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
            when (val result = vehicleRepository.getVehicle(vehicleId)) {
                is VehicleResult.Success -> setState {
                    copy(
                        vehicleName = "${result.data.brand} ${result.data.model}".trim(),
                        plate = result.data.plate
                    )
                }

                is VehicleResult.Failure -> Unit
            }
        }
    }

    private fun loadExistingPhotos(rentalId: String) {
        viewModelScope.launch {
            when (val result = rentalRepository.getRentalPhotos(rentalId)) {
                is RentalResult.Success -> setState {
                    copy(isLoading = false, uploadedSides = result.data.uploadedSides)
                }

                is RentalResult.Failure -> setState {
                    copy(isLoading = false, errorMessage = result.error.toMessage())
                }
            }
        }
    }

    private fun handlePhotoSelected(side: RentalPhotoSide, uri: Uri) {
        if (state.value.mode == RentalPhotoUploadMode.RETURN_TRIP) {
            setState { copy(photos = photos + (side to uri), errorMessage = null) }
            return
        }

        if (state.value.uploadingSide != null) return

        setState { copy(uploadingSide = side, photos = photos + (side to uri), errorMessage = null) }
        viewModelScope.launch {
            when (val result = rentalRepository.uploadRentalPhoto(state.value.rentalId, side, uri)) {
                is RentalResult.Success -> setState {
                    copy(uploadingSide = null, uploadedSides = result.data.uploadedSides)
                }

                is RentalResult.Failure -> setState {
                    copy(
                        uploadingSide = null,
                        photos = photos - side,
                        errorMessage = result.error.toMessage()
                    )
                }
            }
        }
    }

    private fun handlePrimaryActionClicked() {
        val currentState = state.value
        if (!currentState.canSubmit) return

        setState { copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (currentState.mode) {
                RentalPhotoUploadMode.START_TRIP -> startTrip(currentState.rentalId, currentState.vehicleId)
                RentalPhotoUploadMode.RETURN_TRIP -> finishTrip(currentState.rentalId)
            }
        }
    }

    private suspend fun startTrip(rentalId: String, vehicleId: String) {
        when (val result = rentalRepository.startRental(rentalId)) {
            is RentalResult.Success -> {
                setState { copy(isSubmitting = false) }
                sendEffect { RentalPhotoUploadEffect.NavigateToActiveRental(rentalId, vehicleId) }
            }

            is RentalResult.Failure -> setState {
                copy(isSubmitting = false, errorMessage = result.error.toMessage())
            }
        }
    }

    private suspend fun finishTrip(rentalId: String) {
        when (val result = rentalRepository.finishRental(rentalId)) {
            is RentalResult.Success -> {
                setState { copy(isSubmitting = false) }
                sendEffect { RentalPhotoUploadEffect.NavigateHome }
            }

            is RentalResult.Failure -> setState {
                copy(isSubmitting = false, errorMessage = result.error.toMessage())
            }
        }
    }

    private fun handleBackClicked() {
        val currentState = state.value
        if (currentState.mode == RentalPhotoUploadMode.RETURN_TRIP || currentState.rentalId.isBlank()) {
            sendEffect { RentalPhotoUploadEffect.NavigateBack }
            return
        }

        viewModelScope.launch {
            // PREPARING dışındaki (409) durumlarda da geri navigasyon tamamlanmalı; kullanıcı
            // deneyimini bozmamak için hata sessizce yutulur.
            rentalRepository.cancelRental(currentState.rentalId)
            sendEffect { RentalPhotoUploadEffect.NavigateBack }
        }
    }

    private fun RentalError.toMessage(): String = when (this) {
        RentalError.InvalidRequest -> INVALID_REQUEST_MESSAGE
        RentalError.Unauthorized -> UNAUTHORIZED_MESSAGE
        RentalError.Forbidden -> FORBIDDEN_MESSAGE
        RentalError.NotFound -> NOT_FOUND_MESSAGE
        RentalError.Conflict -> CONFLICT_MESSAGE
        RentalError.Network -> NETWORK_ERROR_MESSAGE
        RentalError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val INVALID_REQUEST_MESSAGE = "Fotoğraf yüklenemedi. Lütfen tekrar deneyin."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val FORBIDDEN_MESSAGE = "Bu kiralama size ait değil."
        const val NOT_FOUND_MESSAGE = "Kiralama bulunamadı."
        const val CONFLICT_MESSAGE = "Bu işlem şu anda yapılamıyor."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
