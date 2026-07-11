package com.turkcell.rencar.presentation.screen.reservation.confirmation

import com.turkcell.rencar.presentation.core.mvi.UiState

data class ReservationConfirmationState(
    val vehicleId: String = "",
    val vehicleName: String = "",
    val plate: String = "",
    val vehicleType: String = "",
    val pricePerDay: Double = 0.0,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val termsAccepted: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: ReservationConfirmationError? = null
) : UiState {
    val canComplete: Boolean
        get() = hasLoaded && termsAccepted && !isLoading && !isSubmitting
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
