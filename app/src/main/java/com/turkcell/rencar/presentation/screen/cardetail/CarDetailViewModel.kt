package com.turkcell.rencar.presentation.screen.cardetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.Rental
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.rental.RentalStatus
import com.turkcell.rencar.domain.reservation.Reservation
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
    savedStateHandle: SavedStateHandle
) : MviViewModel<CarDetailState, CarDetailIntent, CarDetailEffect>(
    CarDetailState(
        vehicleId = savedStateHandle.get<String>(RenCarDestination.ARG_VEHICLE_ID).orEmpty(),
        myLatitude = savedStateHandle.get<String>(RenCarDestination.ARG_MY_LATITUDE)?.toDoubleOrNull(),
        myLongitude = savedStateHandle.get<String>(RenCarDestination.ARG_MY_LONGITUDE)?.toDoubleOrNull(),
        reservationUnlockPlan = savedStateHandle.get<String>(RenCarDestination.ARG_RENTAL_PLAN).toRentalPlanOrNull()
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
        }
    }

    private fun handleUnlockClicked() {
        val currentState = state.value
        if (currentState.isUnlocking) return

        val rentalId = currentState.unlockRentalId
        val rentalStatus = currentState.unlockRentalStatus
        if (rentalId != null && rentalStatus != null) {
            when (rentalStatus) {
                RentalStatus.PREPARING -> sendEffect {
                    CarDetailEffect.NavigateToRentalPhotoUpload(rentalId, currentState.vehicleId)
                }

                RentalStatus.ACTIVE -> sendEffect {
                    CarDetailEffect.NavigateToActiveRental(rentalId, currentState.vehicleId)
                }

                else -> Unit
            }
            return
        }

        val plan = currentState.reservationUnlockPlan
        if (currentState.isActiveReservationVehicle && plan != null) {
            createRentalFromReservation(currentState.vehicleId, plan)
        }
    }

    private fun createRentalFromReservation(vehicleId: String, plan: RentalPlan) {
        setState { copy(isUnlocking = true, unlockErrorMessage = null) }
        viewModelScope.launch {
            val endDate = if (plan == RentalPlan.DAILY) oneDayFromNowIsoUtc() else null
            when (val result = rentalRepository.createRental(vehicleId, plan, endDate)) {
                is RentalResult.Success -> handleRentalCreated(result.data, vehicleId)
                is RentalResult.Failure -> setState {
                    copy(isUnlocking = false, unlockErrorMessage = result.error.toUnlockMessage())
                }
            }
        }
    }

    private fun handleRentalCreated(rental: Rental, vehicleId: String) {
        val rentalStatus = rental.status.toRentalStatusOrNull()
        setState {
            copy(
                isUnlocking = false,
                unlockRentalId = rental.id,
                unlockRentalStatus = rentalStatus,
                canUnlock = rentalStatus in UNLOCKABLE_RENTAL_STATUSES
            )
        }
        when (rentalStatus) {
            RentalStatus.PREPARING -> sendEffect {
                CarDetailEffect.NavigateToRentalPhotoUpload(rental.id, vehicleId)
            }

            RentalStatus.ACTIVE -> sendEffect {
                CarDetailEffect.NavigateToActiveRental(rental.id, vehicleId)
            }

            else -> setState { copy(unlockErrorMessage = UNEXPECTED_UNLOCK_MESSAGE) }
        }
    }

    private fun navigateToReservationConfirmation() {
        val currentState = state.value
        if (
            !currentState.hasLoaded ||
            currentState.errorMessage != null ||
            currentState.vehicleId.isBlank() ||
            currentState.isActiveReservationVehicle ||
            currentState.activeReservationVehicleId != null
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
                        vehicleLongitude = result.data.longitude,
                        hasFullVehicleDetails = true,
                        isActiveReservationVehicle = activeReservationVehicleId == result.data.id
                    )
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
            activeReservationVehicleId = reservation.vehicleId
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
                    if (match != null) {
                        setState {
                            copy(
                                canUnlock = true,
                                unlockRentalId = match.id,
                                unlockRentalStatus = match.status,
                                unlockErrorMessage = null
                            )
                        }
                    } else {
                        loadActiveReservationUnlockState()
                    }
                }

                is RentalResult.Failure -> loadActiveReservationUnlockState()
            }
        }
    }

    private suspend fun loadActiveReservationUnlockState() {
        when (val result = reservationRepository.getActiveReservation()) {
            is ReservationResult.Success -> {
                val reservation = result.data
                if (reservation.vehicleId == state.value.vehicleId) {
                    setState {
                        copy(
                            canUnlock = reservationUnlockPlan != null,
                            unlockRentalId = null,
                            unlockRentalStatus = null,
                            isActiveReservationVehicle = true,
                            activeReservationVehicleId = reservation.vehicleId,
                            status = VehicleStatus.RESERVED,
                            unlockErrorMessage = null
                        )
                    }
                } else {
                    setState {
                        copy(
                            canUnlock = false,
                            unlockRentalId = null,
                            unlockRentalStatus = null,
                            activeReservationVehicleId = reservation.vehicleId
                        )
                    }
                }
            }

            is ReservationResult.Failure -> setState {
                copy(
                    canUnlock = false,
                    unlockRentalId = null,
                    unlockRentalStatus = null,
                    activeReservationVehicleId = null
                )
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
        RentalError.InvalidRequest -> INVALID_UNLOCK_MESSAGE
        RentalError.Unauthorized, RentalError.Forbidden -> UNAUTHORIZED_MESSAGE
        RentalError.NotFound -> NOT_FOUND_MESSAGE
        RentalError.Conflict -> CONFLICT_UNLOCK_MESSAGE
        RentalError.Network -> NETWORK_ERROR_MESSAGE
        RentalError.Unexpected -> UNEXPECTED_UNLOCK_MESSAGE
    }

    private fun oneDayFromNowIsoUtc(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { add(Calendar.DAY_OF_YEAR, 1) }
        return SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(calendar.time)
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermis. Lutfen tekrar giris yapin."
        const val NOT_FOUND_MESSAGE = "Bu arac artik musait degil."
        const val NETWORK_ERROR_MESSAGE = "Internet baglantinizi kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Arac bilgileri yuklenemedi. Lutfen tekrar deneyin."
        const val INVALID_UNLOCK_MESSAGE = "Kiralama baslatilamadi. Lutfen bilgileri kontrol edin."
        const val CONFLICT_UNLOCK_MESSAGE = "Bu rezervasyon kiralamaya donusturulemedi. Lutfen tekrar deneyin."
        const val UNEXPECTED_UNLOCK_MESSAGE = "Kiralama baslatilamadi. Lutfen tekrar deneyin."
        const val ISO_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val UNLOCKABLE_RENTAL_STATUSES = setOf(RentalStatus.PREPARING, RentalStatus.ACTIVE)
    }
}

private fun String?.toRentalPlanOrNull(): RentalPlan? =
    this?.takeIf { it.isNotBlank() }?.let { value ->
        runCatching { RentalPlan.valueOf(value) }.getOrNull()
    }

private fun String?.toRentalStatusOrNull(): RentalStatus? =
    this?.let { value -> runCatching { RentalStatus.valueOf(value) }.getOrNull() }
