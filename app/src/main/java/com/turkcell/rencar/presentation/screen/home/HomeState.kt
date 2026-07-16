package com.turkcell.rencar.presentation.screen.home

import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.presentation.component.map.VehicleMarker
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.UiState

data class HomeState(
    val myLocation: LatLng? = null,
    // null: izin sonucu henüz gelmedi, true: reddedildi, false: verildi.
    val permissionDenied: Boolean? = null,
    // false: izin kalıcı reddedildi, sistem diyaloğu artık gösterilemez; Ayarlar'a yönlendirilmeli.
    val canRequestPermission: Boolean = true,
    val vehicles: List<VehicleMarker> = emptyList(),
    val isCheckingActiveReservation: Boolean = false,
    val hasCheckedActiveReservation: Boolean = false,
    val activeReservationVehicleId: String? = null,
    val activeReservationVehicleName: String = "",
    val activeReservationMarker: VehicleMarker? = null,
    val activeReservationErrorMessage: String? = null,
    val isCheckingActiveRental: Boolean = false,
    val hasCheckedActiveRental: Boolean = false,
    val activeRentalId: String? = null,
    val activeRentalVehicleId: String? = null,
    val activeRentalVehicleName: String = "",
    val isVehiclesLoading: Boolean = false,
    val hasLoadedVehicles: Boolean = false,
    val vehiclesErrorMessage: String? = null,
    val selectedCategory: VehicleType? = null,
    val locationLabel: String = "Kadıköy çevresinde",
    val distanceLabel: String = "3 dk uzaklıkta",
    val selectedNavItem: BottomNavItem = BottomNavItem.Map
) : UiState
