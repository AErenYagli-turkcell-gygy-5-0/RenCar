package com.turkcell.rencar.presentation.screen.rental.active

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val vehicleRepository: VehicleRepository
) : MviViewModel<ActiveRentalState, ActiveRentalIntent, ActiveRentalEffect>(ActiveRentalState()) {

    private var pollingJob: Job? = null

    override fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.ScreenStarted -> start(intent.rentalId, intent.vehicleId)
            ActiveRentalIntent.FinishClicked -> handleFinishClicked()
        }
    }

    private fun start(rentalId: String, vehicleId: String) {
        if (state.value.rentalId == rentalId && pollingJob?.isActive == true) return

        setState {
            copy(rentalId = rentalId, vehicleId = vehicleId, isLoading = true, errorMessage = null)
        }
        loadVehicle(vehicleId)
        startPolling()
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

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                refreshActiveRental()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun refreshActiveRental() {
        when (val result = rentalRepository.getActiveRental()) {
            is RentalResult.Success -> setState {
                copy(
                    isLoading = false,
                    elapsedSeconds = result.data.elapsedSeconds,
                    currentCost = result.data.currentCost,
                    distanceKm = result.data.distanceKm,
                    errorMessage = null
                )
            }

            is RentalResult.Failure -> setState {
                copy(isLoading = false, errorMessage = result.error.toMessage())
            }
        }
    }

    private fun handleFinishClicked() {
        if (state.value.isFinishing) return
        pollingJob?.cancel()
        setState { copy(isFinishing = true) }
        sendEffect {
            ActiveRentalEffect.NavigateToFinishPhotoUpload(state.value.rentalId, state.value.vehicleId)
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
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
        const val POLL_INTERVAL_MS = 5_000L
        const val INVALID_REQUEST_MESSAGE = "Bilgiler alınamadı. Lütfen tekrar deneyin."
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermiş. Lütfen tekrar giriş yapın."
        const val FORBIDDEN_MESSAGE = "Bu kiralama size ait değil."
        const val NOT_FOUND_MESSAGE = "Aktif kiralama bulunamadı."
        const val CONFLICT_MESSAGE = "Bu işlem şu anda yapılamıyor."
        const val NETWORK_ERROR_MESSAGE = "İnternet bağlantınızı kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Bir hata oluştu. Lütfen tekrar deneyin."
    }
}
