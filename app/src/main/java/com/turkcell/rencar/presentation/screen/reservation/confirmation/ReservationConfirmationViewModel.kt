package com.turkcell.rencar.presentation.screen.reservation.confirmation

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.rental.RentalError
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.rental.RentalRepository
import com.turkcell.rencar.domain.rental.RentalResult
import com.turkcell.rencar.domain.reservation.ReservationError
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
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
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository
) : MviViewModel<ReservationConfirmationState, ReservationConfirmationIntent, ReservationConfirmationEffect>(
    ReservationConfirmationState()
) {

    override fun onIntent(intent: ReservationConfirmationIntent) {
        when (intent) {
            is ReservationConfirmationIntent.ScreenStarted -> start(intent.vehicleId)
            ReservationConfirmationIntent.BackClicked -> sendEffect { ReservationConfirmationEffect.NavigateBack }
            ReservationConfirmationIntent.RetryClicked -> loadVehicle(state.value.vehicleId)
            is ReservationConfirmationIntent.PlanSelected -> selectPlan(intent.plan)
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
        setState { copy(vehicleId = vehicleId, isLoading = true, hasLoaded = false, hasQuote = false, error = null) }
        viewModelScope.launch {
            when (val result = vehicleRepository.getVehicle(vehicleId)) {
                is VehicleResult.Success -> {
                    setState {
                        copy(
                            vehicleName = "${result.data.brand} ${result.data.model}".trim(),
                            plate = result.data.plate,
                            transmission = result.data.transmission,
                            seats = result.data.seats,
                            fuelPercent = result.data.fuelPercent,
                            pricePerMinute = result.data.pricePerMinute,
                            pricePerHour = result.data.pricePerHour,
                            pricePerDay = result.data.pricePerDay,
                            isLoading = false,
                            hasLoaded = true,
                            error = null
                        )
                    }
                    loadQuote(state.value.selectedPlan)
                }
                is VehicleResult.Failure -> setState {
                    copy(isLoading = false, hasLoaded = false, error = result.error.toPresentationError())
                }
            }
        }
    }

    private fun selectPlan(plan: RentalPlan) {
        if (plan == state.value.selectedPlan || !state.value.hasLoaded) return
        setState { copy(selectedPlan = plan, hasQuote = false, error = null) }
        loadQuote(plan)
    }

    private fun loadQuote(plan: RentalPlan) {
        val vehicleId = state.value.vehicleId
        val minutes = state.value.quoteMinutes
        if (vehicleId.isBlank()) return
        setState { copy(isQuoteLoading = true, hasQuote = false, error = null) }
        viewModelScope.launch {
            when (val result = vehicleRepository.getQuote(vehicleId, plan.name, minutes)) {
                is VehicleResult.Success -> if (state.value.selectedPlan == plan) {
                    setState {
                        copy(
                            usageFee = result.data.usageFee,
                            startFee = result.data.startFee,
                            serviceFee = result.data.serviceFee,
                            estimatedTotal = result.data.estimatedTotal,
                            isQuoteLoading = false,
                            hasQuote = true,
                            error = null
                        )
                    }
                }
                is VehicleResult.Failure -> if (state.value.selectedPlan == plan) {
                    setState {
                        copy(isQuoteLoading = false, hasQuote = false, error = result.error.toPresentationError())
                    }
                }
            }
        }
    }

    private fun completeReservation() {
        val currentState = state.value
        if (!currentState.canComplete) return
        setState { copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            when (val result = reservationRepository.createReservation(currentState.vehicleId)) {
                is ReservationResult.Success -> completeRental(currentState)
                is ReservationResult.Failure -> setState {
                    copy(isSubmitting = false, error = result.error.toPresentationError())
                }
            }
        }
    }

    private suspend fun completeRental(currentState: ReservationConfirmationState) {
        val endDate = if (currentState.selectedPlan == RentalPlan.DAILY) oneDayFromNowIsoUtc() else null
        when (val result = rentalRepository.createRental(currentState.vehicleId, currentState.selectedPlan, endDate)) {
            is RentalResult.Success -> {
                setState { copy(isSubmitting = false) }
                sendEffect {
                    ReservationConfirmationEffect.ReservationCreated(
                        rentalId = result.data.id,
                        vehicleId = currentState.vehicleId,
                        isPreparing = result.data.status == RENTAL_STATUS_PREPARING
                    )
                }
            }
            is RentalResult.Failure -> setState {
                copy(isSubmitting = false, error = result.error.toPresentationError())
            }
        }
    }

    private fun VehicleError.toPresentationError() = when (this) {
        VehicleError.Unauthorized -> ReservationConfirmationError.UNAUTHORIZED
        VehicleError.Forbidden -> ReservationConfirmationError.FORBIDDEN
        VehicleError.NotFound -> ReservationConfirmationError.VEHICLE_NOT_FOUND
        VehicleError.Network -> ReservationConfirmationError.NETWORK
        VehicleError.Unexpected -> ReservationConfirmationError.UNEXPECTED
    }

    private fun RentalError.toPresentationError() = when (this) {
        RentalError.InvalidRequest -> ReservationConfirmationError.INVALID_REQUEST
        RentalError.Unauthorized -> ReservationConfirmationError.UNAUTHORIZED
        RentalError.Forbidden -> ReservationConfirmationError.FORBIDDEN
        RentalError.NotFound -> ReservationConfirmationError.VEHICLE_NOT_FOUND
        RentalError.Conflict -> ReservationConfirmationError.RESERVATION_CONFLICT
        RentalError.Network -> ReservationConfirmationError.NETWORK
        RentalError.Unexpected -> ReservationConfirmationError.UNEXPECTED
    }

    private fun ReservationError.toPresentationError() = when (this) {
        ReservationError.InvalidRequest -> ReservationConfirmationError.INVALID_REQUEST
        ReservationError.Unauthorized -> ReservationConfirmationError.UNAUTHORIZED
        ReservationError.Forbidden -> ReservationConfirmationError.FORBIDDEN
        ReservationError.NotFound -> ReservationConfirmationError.VEHICLE_NOT_FOUND
        ReservationError.Conflict -> ReservationConfirmationError.RESERVATION_CONFLICT
        ReservationError.Network -> ReservationConfirmationError.NETWORK
        ReservationError.Unexpected -> ReservationConfirmationError.UNEXPECTED
    }

    private fun oneDayFromNowIsoUtc(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { add(Calendar.DAY_OF_YEAR, 1) }
        return SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(calendar.time)
    }

    private companion object {
        const val ISO_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val RENTAL_STATUS_PREPARING = "PREPARING"
    }
}
