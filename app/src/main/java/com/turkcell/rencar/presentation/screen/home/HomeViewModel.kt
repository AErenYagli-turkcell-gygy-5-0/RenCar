package com.turkcell.rencar.presentation.screen.home

import androidx.lifecycle.viewModelScope
import com.turkcell.rencar.domain.reservation.ReservationError
import com.turkcell.rencar.domain.reservation.ReservationRepository
import com.turkcell.rencar.domain.reservation.ReservationResult
import com.turkcell.rencar.domain.vehicle.Vehicle
import com.turkcell.rencar.domain.vehicle.VehicleError
import com.turkcell.rencar.domain.vehicle.VehicleRepository
import com.turkcell.rencar.domain.vehicle.VehicleResult
import com.turkcell.rencar.presentation.component.map.VehicleMarker
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val reservationRepository: ReservationRepository
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.ScreenStarted -> {
                if (!state.value.hasCheckedActiveReservation) {
                    checkActiveReservation()
                } else if (!state.value.hasLoadedVehicles && state.value.activeReservationErrorMessage == null) {
                    loadVehicles()
                }
            }

            HomeIntent.RetryVehiclesClicked -> {
                if (state.value.activeReservationErrorMessage != null) {
                    checkActiveReservation()
                } else {
                    loadVehicles()
                }
            }

            HomeIntent.RefreshMapClicked -> {
                if (state.value.activeReservationErrorMessage != null) {
                    checkActiveReservation()
                } else {
                    loadVehicles()
                }
            }

            is HomeIntent.LocationPermissionResult ->
                setState {
                    copy(
                        permissionDenied = intent.granted.not(),
                        canRequestPermission = intent.canRequestAgain
                    )
                }

            is HomeIntent.MyLocationChanged ->
                setState { copy(myLocation = intent.location) }

            // Route tarafından yakalanıp izin isteme launcher'ına çevrilir (bkz. LicenseUploadRoute örüntüsü).
            HomeIntent.RequestLocationPermissionClicked -> Unit

            is HomeIntent.CategorySelected ->
                setState { copy(selectedCategory = intent.category) }

            // Konum izni verilmeden (veya henüz konum alınmadan) en yakın araç aranmaz; buton sessizce hiçbir şey yapmaz.
            HomeIntent.FindNearestClicked -> {
                val locationGranted = state.value.permissionDenied == false
                val myLocation = state.value.myLocation
                if (locationGranted && myLocation != null) {
                    val visibleVehicles = state.value.vehicles.filter { vehicle ->
                        state.value.selectedCategory == null || vehicle.category == state.value.selectedCategory
                    }
                    val nearestVehicle = visibleVehicles.minByOrNull { vehicle ->
                        haversineMeters(myLocation.latitude, myLocation.longitude, vehicle.latitude, vehicle.longitude)
                    }
                    // CarDetailScreen açılışta kendi haritasını bu aracın konumuna zoom'lar (bkz. docs/decisions.md, 2026-07-10).
                    nearestVehicle?.let { vehicle ->
                        sendEffect { HomeEffect.NavigateToCarDetail(vehicle.id, myLocation) }
                    }
                }
            }

            is HomeIntent.NavItemSelected -> {
                // Konum izni verilmeden harita dışındaki ekranlara geçiş engellenir (bkz. docs/decisions.md).
                val locationGranted = state.value.permissionDenied == false
                if (!locationGranted) {
                    Unit
                } else if (intent.item == BottomNavItem.Profile) {
                    sendEffect { HomeEffect.NavigateToProfile }
                } else {
                    setState { copy(selectedNavItem = intent.item) }
                }
            }

            is HomeIntent.VehicleMarkerClicked -> {
                // Konum izni verilmeden CarDetail ekranına geçiş engellenir (bkz. docs/decisions.md).
                val locationGranted = state.value.permissionDenied == false
                if (locationGranted) {
                    sendEffect { HomeEffect.NavigateToCarDetail(intent.vehicleId, state.value.myLocation) }
                }
            }
        }
    }

    private fun loadVehicles() {
        if (state.value.isVehiclesLoading) return

        setState { copy(isVehiclesLoading = true, vehiclesErrorMessage = null) }
        viewModelScope.launch {
            when (val result = vehicleRepository.listAvailableVehicles()) {
                is VehicleResult.Success -> setState {
                    copy(
                        vehicles = result.data.map { it.toMarker() },
                        isVehiclesLoading = false,
                        hasLoadedVehicles = true
                    )
                }

                is VehicleResult.Failure -> setState {
                    copy(
                        isVehiclesLoading = false,
                        hasLoadedVehicles = true,
                        vehiclesErrorMessage = result.error.toMessage()
                    )
                }
            }
        }
    }

    private fun checkActiveReservation() {
        if (state.value.isCheckingActiveReservation) return

        setState {
            copy(
                isCheckingActiveReservation = true,
                activeReservationErrorMessage = null,
                vehiclesErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = reservationRepository.getActiveReservation()) {
                is ReservationResult.Success -> {
                    setState {
                        copy(
                            isCheckingActiveReservation = false,
                            hasCheckedActiveReservation = true
                        )
                    }
                    sendEffect {
                        HomeEffect.NavigateToActiveReservationCarDetail(result.data.vehicleId)
                    }
                }

                is ReservationResult.Failure -> {
                    if (result.error == ReservationError.NotFound) {
                        setState {
                            copy(
                                isCheckingActiveReservation = false,
                                hasCheckedActiveReservation = true,
                                activeReservationErrorMessage = null
                            )
                        }
                        loadVehicles()
                    } else {
                        setState {
                            copy(
                                isCheckingActiveReservation = false,
                                hasCheckedActiveReservation = true,
                                hasLoadedVehicles = true,
                                isVehiclesLoading = false,
                                activeReservationErrorMessage = result.error.toMessage()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun Vehicle.toMarker() = VehicleMarker(
        id = id,
        latitude = latitude,
        longitude = longitude,
        price = pricePerDay.toInt(),
        category = type
    )

    private fun VehicleError.toMessage(): String = when (this) {
        VehicleError.Unauthorized, VehicleError.Forbidden -> UNAUTHORIZED_MESSAGE
        VehicleError.Network -> NETWORK_ERROR_MESSAGE
        VehicleError.NotFound, VehicleError.Unexpected -> UNEXPECTED_ERROR_MESSAGE
    }

    private fun ReservationError.toMessage(): String = when (this) {
        ReservationError.Unauthorized, ReservationError.Forbidden -> UNAUTHORIZED_MESSAGE
        ReservationError.Network -> NETWORK_ERROR_MESSAGE
        ReservationError.InvalidRequest,
        ReservationError.Conflict,
        ReservationError.NotFound,
        ReservationError.Unexpected -> ACTIVE_RESERVATION_ERROR_MESSAGE
    }

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermis. Lutfen tekrar giris yapin."
        const val NETWORK_ERROR_MESSAGE = "Internet baglantinizi kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Araclar yuklenemedi. Lutfen tekrar deneyin."
        const val ACTIVE_RESERVATION_ERROR_MESSAGE =
            "Aktif rezervasyon durumu kontrol edilemedi. Lutfen tekrar deneyin."
    }
}

// CarDetailScreen.kt'deki private haversineMeters ile ayni formul; ekranlar birbirinden bagimsiz
// oldugundan (bkz. docs/decisions.md) burada da ayni sekilde ozel/private tutulmustur.
private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
        kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
        kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadiusMeters * c
}
