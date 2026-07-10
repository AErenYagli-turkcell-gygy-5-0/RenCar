package com.turkcell.rencar.presentation.screen.home

import androidx.lifecycle.viewModelScope
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
    private val vehicleRepository: VehicleRepository
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.ScreenStarted -> {
                if (!state.value.hasLoadedVehicles) loadVehicles()
            }

            HomeIntent.RetryVehiclesClicked -> loadVehicles()

            HomeIntent.RefreshMapClicked -> loadVehicles()

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

            // Gerçek arama/filtreleme mantığı backend entegrasyonu ile birlikte eklenecektir.
            HomeIntent.FindNearestClicked -> Unit

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

    private companion object {
        const val UNAUTHORIZED_MESSAGE = "Oturumunuz sona ermis. Lutfen tekrar giris yapin."
        const val NETWORK_ERROR_MESSAGE = "Internet baglantinizi kontrol edip tekrar deneyin."
        const val UNEXPECTED_ERROR_MESSAGE = "Araclar yuklenemedi. Lutfen tekrar deneyin."
    }
}
