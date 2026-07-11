package com.turkcell.rencar.presentation.screen.reservation.confirmation

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ReservationConfirmationViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository
) : MviViewModel<ReservationConfirmationState, ReservationConfirmationIntent, ReservationConfirmationEffect>(
    ReservationConfirmationState()
) {

    override fun onIntent(intent: ReservationConfirmationIntent) {
        when (intent) {
            is ReservationConfirmationIntent.ScreenStarted -> start(intent.vehicleId)
            ReservationConfirmationIntent.BackClicked ->
                sendEffect { ReservationConfirmationEffect.NavigateBack }

            ReservationConfirmationIntent.RetryClicked -> loadVehicle(state.value.vehicleId)
            is ReservationConfirmationIntent.TermsAcceptanceChanged ->
                setState { copy(termsAccepted = intent.accepted) }

            ReservationConfirmationIntent.CompleteReservationClicked -> completeReservation()
        }
    }

    private fun start(vehicleId: String) {
        if (vehicleId.isBlank()) {
            setState { copy(hasLoaded = false, error = ReservationConfirmationError.VEHICLE_NOT_FOUND) }
            return
        }
        if (state.value.hasLoaded && state.value.vehicleId == vehicleId) return
        loadVehicle(vehicleId)
    }

    private fun loadVehicle(vehicleId: String) {
        if (state.value.isLoading || vehicleId.isBlank()) return

        setState {
            copy(
                vehicleId = vehicleId,
                isLoading = true,
                hasLoaded = false,
                error = null
            )
        }
        viewModelScope.launch {
            when (val result = vehicleRepository.getVehicle(vehicleId)) {
                is VehicleResult.Success -> setState {
                    copy(
                        vehicleName = "${result.data.brand} ${result.data.model}".trim(),
                        plate = result.data.plate,
                        vehicleType = result.data.type.toDisplayName(),
                        pricePerDay = result.data.pricePerDay,
                        isLoading = false,
                        hasLoaded = true,
                        error = null
                    )
                }

                is VehicleResult.Failure -> setState {
                    copy(
                        isLoading = false,
                        hasLoaded = false,
                        error = result.error.toPresentationError()
                    )
                }
            }
        }
    }

    private fun completeReservation() {
        val currentState = state.value
        if (!currentState.canComplete) return

        setState { copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            when (
                val result = rentalRepository.createRental(
                    vehicleId = currentState.vehicleId,
                    endDate = oneDayFromNowIsoUtc()
                )
            ) {
                is RentalResult.Success -> {
                    setState { copy(isSubmitting = false) }
                    sendEffect { ReservationConfirmationEffect.ReservationCreated(result.data.id) }
                }

                is RentalResult.Failure -> setState {
                    copy(
                        isSubmitting = false,
                        error = result.error.toPresentationError()
                    )
                }
            }
        }
    }

    private fun VehicleError.toPresentationError(): ReservationConfirmationError = when (this) {
        VehicleError.Unauthorized -> ReservationConfirmationError.UNAUTHORIZED
        VehicleError.Forbidden -> ReservationConfirmationError.FORBIDDEN
        VehicleError.NotFound -> ReservationConfirmationError.VEHICLE_NOT_FOUND
        VehicleError.Network -> ReservationConfirmationError.NETWORK
        VehicleError.Unexpected -> ReservationConfirmationError.UNEXPECTED
    }

    private fun RentalError.toPresentationError(): ReservationConfirmationError = when (this) {
        RentalError.InvalidRequest -> ReservationConfirmationError.INVALID_REQUEST
        RentalError.Unauthorized -> ReservationConfirmationError.UNAUTHORIZED
        RentalError.Forbidden -> ReservationConfirmationError.FORBIDDEN
        RentalError.NotFound -> ReservationConfirmationError.VEHICLE_NOT_FOUND
        RentalError.Conflict -> ReservationConfirmationError.RESERVATION_CONFLICT
        RentalError.Network -> ReservationConfirmationError.NETWORK
        RentalError.Unexpected -> ReservationConfirmationError.UNEXPECTED
    }

    private fun com.turkcell.rencar.domain.vehicle.VehicleType.toDisplayName(): String =
        name.lowercase().replaceFirstChar { it.titlecase(Locale.forLanguageTag("tr-TR")) }

    private fun oneDayFromNowIsoUtc(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(calendar.time)
    }

    private companion object {
        const val ISO_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }
}
