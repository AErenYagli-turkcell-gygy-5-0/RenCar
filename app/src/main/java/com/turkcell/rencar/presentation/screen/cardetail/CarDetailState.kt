package com.turkcell.rencar.presentation.screen.cardetail

import com.turkcell.rencar.domain.vehicle.Transmission
import com.turkcell.rencar.domain.vehicle.VehicleSegment
import com.turkcell.rencar.domain.vehicle.VehicleStatus
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.presentation.core.mvi.UiState

data class CarDetailState(
    val vehicleId: String = "",
    val myLatitude: Double? = null,
    val myLongitude: Double? = null,
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val type: VehicleType = VehicleType.SEDAN,
    val pricePerDay: Double = 0.0,
    val pricePerMinute: Double = 0.0,
    val pricePerHour: Double = 0.0,
    val fuelPercent: Double = 0.0,
    val rangeKm: Double = 0.0,
    val transmission: Transmission = Transmission.MANUAL,
    val seats: Int = 0,
    val segment: VehicleSegment = VehicleSegment.ECONOMY,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val vehicleLatitude: Double = 0.0,
    val vehicleLongitude: Double = 0.0,
    val hasFullVehicleDetails: Boolean = false,
    val isActiveReservationVehicle: Boolean = false,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null,
    val canUnlock: Boolean = false
) : UiState
