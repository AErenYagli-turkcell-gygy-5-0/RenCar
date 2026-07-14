package com.turkcell.rencar.presentation.screen.reservation.confirmation

import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.presentation.core.mvi.UiState

data class ReservationConfirmationState(
    val vehicleId: String = "",
    val vehicleName: String = "",
    val plate: String = "",
    val transmission: Transmission = Transmission.MANUAL,
    val seats: Int = 0,
    val fuelPercent: Double = 0.0,
    val pricePerMinute: Double = 0.0,
    val pricePerHour: Double = 0.0,
    val pricePerDay: Double = 0.0,
    val selectedPlan: RentalPlan = RentalPlan.PER_MINUTE,
    val quoteMinutes: Int = 30,
    val startFee: Double = 0.0,
    val estimatedTotal: Double = 0.0,
    val hasQuote: Boolean = false,
    val isQuoteLoading: Boolean = false,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val termsAccepted: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: ReservationConfirmationError? = null
) : UiState {
    val canComplete: Boolean
        get() = hasLoaded && hasQuote && termsAccepted && !isLoading && !isQuoteLoading && !isSubmitting
}

enum class ReservationConfirmationError {
    VEHICLE_NOT_FOUND,
    RESERVATION_CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN,
    INVALID_REQUEST,
    NETWORK,
    UNEXPECTED
}
