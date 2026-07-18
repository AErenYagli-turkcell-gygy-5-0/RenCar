package com.turkcell.rencar.presentation.screen.cardetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalStatus
import com.turkcell.rencar.domain.reservation.Reservation
import com.turkcell.rencar.domain.reservation.ReservationPlanStore
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.domain.vehicle.VehicleStatus
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import com.turkcell.rencar.presentation.navigation.RenCarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    private val reservationPlanStore: ReservationPlanStore,
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
            CarDetailIntent.UnlockClicked -> handleUnlockClicked()
            CarDetailIntent.CancelReservationClicked -> handleCancelReservationClicked()
        }
    }

    private fun handleUnlockClicked() {
        val currentState = state.value
        if (currentState.isUnlockSubmitting) return

        val rentalId = currentState.unlockRentalId
        if (rentalId == null) {
            if (currentState.isActiveReservationVehicle && currentState.canUnlock) {
                createRentalFromActiveReservation(currentState.vehicleId)
            }
            return
        }

        when (currentState.unlockRentalStatus) {
            RentalStatus.PREPARING -> sendEffect {
                CarDetailEffect.NavigateToRentalPhotoUpload(rentalId, currentState.vehicleId)
            }

            RentalStatus.ACTIVE -> sendEffect {
                CarDetailEffect.NavigateToActiveRental(rentalId, currentState.vehicleId)
            }

            else -> Unit
        }
    }

    private fun createRentalFromActiveReservation(vehicleId: String) {
        if (vehicleId.isBlank()) return
        setState { copy(isUnlockSubmitting = true, unlockErrorMessage = null) }
        viewModelScope.launch {
            val selectedPlan = reservationPlanStore.getPlan(vehicleId) ?: RentalPlan.PER_MINUTE
            val endDate = if (selectedPlan == RentalPlan.DAILY) oneDayFromNowIsoUtc() else null
            when (val result = rentalRepository.createRental(vehicleId, selectedPlan, endDate = endDate)) {
                is RentalResult.Success -> {
                    reservationPlanStore.clearPlan(vehicleId)
                    setState { copy(isUnlockSubmitting = false) }
                    when (result.data.status) {
                        RENTAL_STATUS_PREPARING -> sendEffect {
                            CarDetailEffect.NavigateToRentalPhotoUpload(result.data.id, vehicleId)
                        }

                        RENTAL_STATUS_ACTIVE -> sendEffect {
                            CarDetailEffect.NavigateToActiveRental(result.data.id, vehicleId)
                        }

                        else -> setState { copy(unlockErrorMessage = UNEXPECTED_RENTAL_STATUS_MESSAGE) }
                    }
                }

                is RentalResult.Failure -> setState {
                    copy(isUnlockSubmitting = false, unlockErrorMessage = result.error.toUnlockMessage())
                }
            }
        }
    }

    private fun handleCancelReservationClicked() {
        val currentState = state.value
        val reservationId = currentState.activeReservationId ?: return
        if (currentState.isCancelReservationSubmitting) return

        setState { copy(isCancelReservationSubmitting = true, cancelReservationErrorMessage = null) }
        viewModelScope.launch {
            when (val result = reservationRepository.cancelReservation(reservationId)) {
                is ReservationResult.Success -> {
                    reservationPlanStore.clearPlan(currentState.vehicleId)
                    setState {
                        copy(
                            isCancelReservationSubmitting = false,
                            isActiveReservationVehicle = false,
                            activeReservationId = null,
                            status = VehicleStatus.AVAILABLE,
                            canUnlock = false,
                            unlockRentalId = null,
                            unlockRentalStatus = null,
                            unlockErrorMessage = null,
                            cancelReservationErrorMessage = null
                        )
                    }
                }

                is ReservationResult.Failure -> setState {
                    copy(
                        isCancelReservationSubmitting = false,
                        cancelReservationErrorMessage = result.error.toCancelMessage()
                    )
                }
            }
        }
    }

    private fun navigateToReservationConfirmation() {
        val currentState = state.value
        if (
            !currentState.hasLoaded ||
            currentState.errorMessage != null ||
            currentState.vehicleId.isBlank() ||
            currentState.isActiveReservationVehicle
        ) {
            return
        }

        sendEffect {
            CarDetailEffect.NavigateToReservationConfirmation(currentState.vehicleId)
        }
    }

    private fun loadVehicle() {
        if (state.value.isLoading) return

        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = vehicleRepository.getVehicle(state.value.vehicleId)) {
                is VehicleResult.Success -> {
                    setState {
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
                            vehicleLongitude = result.data.longitude,
                            hasFullVehicleDetails = true,
                            isActiveReservationVehicle = false,
                            activeReservationId = null,
                            cancelReservationErrorMessage = null
                        )
                    }
                    if (result.data.status == VehicleStatus.RESERVED) {
                        markActiveReservationUnlockableIfMatches()
                    }
                }

                is VehicleResult.Failure -> {
                    if (result.error == VehicleError.NotFound) {
                        loadActiveReservationVehicleSummary()
                    } else {
                        setState {
                            copy(
                                isLoading = false,
                                hasLoaded = true,
                                errorMessage = result.error.toMessage()
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun markActiveReservationUnlockableIfMatches() {
        when (val result = reservationRepository.getActiveReservation()) {
            is ReservationResult.Success -> if (result.data.vehicleId == state.value.vehicleId) {
                setState {
                    copy(
                        isActiveReservationVehicle = true,
                        activeReservationId = result.data.id,
                        canUnlock = true,
                        unlockRentalId = null,
                        unlockRentalStatus = null,
                        unlockErrorMessage = null,
                        cancelReservationErrorMessage = null
                    )
                }
            }

            is ReservationResult.Failure -> Unit
        }
    }

    private suspend fun loadActiveReservationVehicleSummary() {
        when (val result = reservationRepository.getActiveReservation()) {
            is ReservationResult.Success -> {
                val reservation = result.data
                if (reservation.vehicleId == state.value.vehicleId) {
                    setState { applyActiveReservation(reservation) }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = VehicleError.NotFound.toMessage()
                        )
                    }
                }
            }

            is ReservationResult.Failure -> setState {
                copy(
                    isLoading = false,
                    hasLoaded = true,
                    errorMessage = VehicleError.NotFound.toMessage()
                )
            }
        }
    }

    private fun CarDetailState.applyActiveReservation(reservation: Reservation): CarDetailState =
        copy(
            isLoading = false,
            hasLoaded = true,
            errorMessage = null,
            brand = reservation.vehicle.brand,
            model = reservation.vehicle.model,
            plate = reservation.vehicle.plate,
            type = reservation.vehicle.type,
            pricePerMinute = reservation.vehicle.pricePerMinute,
            status = VehicleStatus.RESERVED,
            vehicleLatitude = reservation.vehicle.latitude,
            vehicleLongitude = reservation.vehicle.longitude,
            hasFullVehicleDetails = false,
            isActiveReservationVehicle = true,
            activeReservationId = reservation.id,
            canUnlock = true,
            unlockRentalId = null,
            unlockRentalStatus = null,
            unlockErrorMessage = null,
            cancelReservationErrorMessage = null
        )

    // "Kilidi Aç" yalnızca bu araçta PREPARING/ACTIVE bir kiralamamız varsa aktif olmalı.
    // Hata durumunda sessizce pasif kalır.
    private fun loadCanUnlock() {
        viewModelScope.launch {
            when (val result = rentalRepository.getMyRentals()) {
                is RentalResult.Success -> {
                    val currentVehicleId = state.value.vehicleId
                    val match = result.data.firstOrNull {
                        it.vehicleId == currentVehicleId && it.status in UNLOCKABLE_RENTAL_STATUSES
                    }
                    setState {
                        copy(
                            canUnlock = match != null || state.value.isActiveReservationVehicle,
                            unlockRentalId = match?.id,
                            unlockRentalStatus = match?.status
                        )
                    }
                }

                is RentalResult.Failure -> setState {
                    if (isActiveReservationVehicle) {
                        copy(unlockRentalId = null, unlockRentalStatus = null)
                    } else {
                        copy(canUnlock = false, unlockRentalId = null, unlockRentalStatus = null)
                    }
                }
            }
        }
    }

    private fun VehicleError.toMessage(): String = when (this) {
        VehicleError.Unauthorized, VehicleError.Forbidden -> UNAUTHORIZED_MESSAGE
        VehicleError.NotFound -> NOT_FOUND_MESSAGE
        VehicleError.Network -> NETWORK_ERROR_MESSAGE
        VehicleError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private fun RentalError.toUnlockMessage(): String = when (this) {
        RentalError.Unauthorized, RentalError.Forbidden -> UNAUTHORIZED_MESSAGE
        RentalError.NotFound -> NOT_FOUND_MESSAGE
        RentalError.Network -> NETWORK_ERROR_MESSAGE
        RentalError.Conflict -> RESERVATION_CONFLICT_MESSAGE
        RentalError.InvalidRequest, RentalError.Unexpected -> UNEXPECTED_RENTAL_STATUS_MESSAGE
    }

    private fun com.turkcell.rencar.domain.reservation.ReservationError.toCancelMessage(): String = when (this) {
        com.turkcell.rencar.domain.reservation.ReservationError.Unauthorized,
        com.turkcell.rencar.domain.reservation.ReservationError.Forbidden -> UNAUTHORIZED_MESSAGE
        com.turkcell.rencar.domain.reservation.ReservationError.NotFound -> RESERVATION_NOT_FOUND_MESSAGE
        com.turkcell.rencar.domain.reservation.ReservationError.Network -> NETWORK_ERROR_MESSAGE
        com.turkcell.rencar.domain.reservation.ReservationError.Conflict -> RESERVATION_CANCEL_CONFLICT_MESSAGE
        com.turkcell.rencar.domain.reservation.ReservationError.InvalidRequest,
        com.turkcell.rencar.domain.reservation.ReservationError.Unexpected -> RESERVATION_CANCEL_UNEXPECTED_MESSAGE
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermis. Lutfen tekrar giris yapin."
        const val NOT_FOUND_MESSAGE = "Bu arac artik musait degil."
        const val NETWORK_ERROR_MESSAGE = "Internet baglantinizi kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Arac bilgileri yuklenemedi. Lutfen tekrar deneyin."
        const val RESERVATION_CONFLICT_MESSAGE =
            "Rezervasyon suresi dolmus olabilir veya bu aracta kiralama baslatilamiyor."
        const val UNEXPECTED_RENTAL_STATUS_MESSAGE = "Kiralama baslatilamadi. Lutfen tekrar deneyin."
        const val RESERVATION_NOT_FOUND_MESSAGE = "Aktif rezervasyon bulunamadi."
        const val RESERVATION_CANCEL_CONFLICT_MESSAGE = "Rezervasyon iptal edilemiyor veya suresi dolmus."
        const val RESERVATION_CANCEL_UNEXPECTED_MESSAGE = "Rezervasyon iptal edilemedi. Lutfen tekrar deneyin."
        const val RENTAL_STATUS_PREPARING = "PREPARING"
        const val RENTAL_STATUS_ACTIVE = "ACTIVE"
        val UNLOCKABLE_RENTAL_STATUSES = setOf(RentalStatus.PREPARING, RentalStatus.ACTIVE)
    }
}

private fun oneDayFromNowIsoUtc(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { add(Calendar.DAY_OF_YEAR, 1) }
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(calendar.time)
}
