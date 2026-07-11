package com.turkcell.rencar.presentation.screen.cardetail

import com.turkcell.rencar.presentation.core.mvi.UiState

data class CarDetailState(
    val vehicleId: String = "",
    val myLatitude: Double? = null,
    val myLongitude: Double? = null,
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val pricePerDay: Double = 0.0,
    val vehicleLatitude: Double = 0.0,
    val vehicleLongitude: Double = 0.0,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null
) : UiState
