package com.turkcell.rencar.presentation.screen.rental.active

import com.turkcell.rencar.presentation.core.mvi.UiState

data class ActiveRentalState(
    val rentalId: String = "",
    val vehicleId: String = "",
    val vehicleName: String = "",
    val plate: String = "",
    val elapsedSeconds: Long = 0L,
    val currentCost: Double = 0.0,
    val distanceKm: Double = 0.0,
    val isLoading: Boolean = false,
    val isFinishing: Boolean = false,
    val errorMessage: String? = null
) : UiState
