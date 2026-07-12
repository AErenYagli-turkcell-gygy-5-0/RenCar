package com.turkcell.rencar.presentation.screen.cardetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalStatus
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository,
    savedStateHandle: SavedStateHandle
) : MviViewModel<CarDetailState, CarDetailIntent, CarDetailEffect>(
    CarDetailState(
        vehicleId = savedStateHandle.get<String>(RenCarDestination.ARG_VEHICLE_ID).orEmpty(),
        myLatitude = savedStateHandle.get<String>(RenCarDestination.ARG_MY_LATITUDE)?.toDoubleOrNull(),
        myLongitude = savedStateHandle.get<String>(RenCarDestination.ARG_MY_LONGITUDE)?.toDoubleOrNull()
    )
) {

    override fun onIntent(intent: CarDetailIntent) {
        when (intent) {
            CarDetailIntent.ScreenStarted -> {
                if (!state.value.hasLoaded) loadVehicle()
                loadCanUnlock()
            }

            CarDetailIntent.RetryClicked -> loadVehicle()
            CarDetailIntent.BackClicked -> sendEffect { CarDetailEffect.NavigateBack }
            CarDetailIntent.ReserveClicked -> navigateToReservationConfirmation()
        }
    }

    private fun navigateToReservationConfirmation() {
        val currentState = state.value
        if (!currentState.hasLoaded || currentState.errorMessage != null || currentState.vehicleId.isBlank()) return

        sendEffect {
            CarDetailEffect.NavigateToReservationConfirmation(currentState.vehicleId)
        }
    }

    private fun loadVehicle() {
        if (state.value.isLoading) return

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = vehicleRepository.getVehicle(state.value.vehicleId)) {
                is VehicleResult.Success -> setState {
                    copy(
                        isLoading = false,
                        hasLoaded = true,
                        brand = result.data.brand,
                        model = result.data.model,
                        plate = result.data.plate,
                        type = result.data.type,
                        pricePerDay = result.data.pricePerDay,
                        pricePerMinute = result.data.pricePerMinute,
                        pricePerHour = result.data.pricePerHour,
                        fuelPercent = result.data.fuelPercent,
                        rangeKm = result.data.rangeKm,
                        transmission = result.data.transmission,
                        seats = result.data.seats,
                        segment = result.data.segment,
                        status = result.data.status,
                        vehicleLatitude = result.data.latitude,
                        vehicleLongitude = result.data.longitude
                    )
                }

                is VehicleResult.Failure -> setState {
                    copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = result.error.toMessage()
                    )
                }
            }
        }
    }

    // "Kilidi Aç" yalnızca bu araçta PREPARING/ACTIVE bir kiralamamız varsa aktif olmalı;
    // hata durumunda sessizce pasif kalır (buton zaten bugün onClick={} — yalnızca kapatılıyor).
    private fun loadCanUnlock() {
        viewModelScope.launch {
            when (val result = rentalRepository.getMyRentals()) {
                is RentalResult.Success -> setState {
                    copy(
                        canUnlock = result.data.any {
                            it.vehicleId == vehicleId && it.status in UNLOCKABLE_RENTAL_STATUSES
                        }
                    )
                }

                is RentalResult.Failure -> setState { copy(canUnlock = false) }
            }
        }
    }

    private fun VehicleError.toMessage(): String = when (this) {
        VehicleError.Unauthorized, VehicleError.Forbidden -> UNAUTHORIZED_MESSAGE
        VehicleError.NotFound -> NOT_FOUND_MESSAGE
        VehicleError.Network -> NETWORK_ERROR_MESSAGE
        VehicleError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermis. Lutfen tekrar giris yapin."
        const val NOT_FOUND_MESSAGE = "Bu arac artik musait degil."
        const val NETWORK_ERROR_MESSAGE = "Internet baglantinizi kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Arac bilgileri yuklenemedi. Lutfen tekrar deneyin."
        val UNLOCKABLE_RENTAL_STATUSES = setOf(RentalStatus.PREPARING, RentalStatus.ACTIVE)
    }
}
