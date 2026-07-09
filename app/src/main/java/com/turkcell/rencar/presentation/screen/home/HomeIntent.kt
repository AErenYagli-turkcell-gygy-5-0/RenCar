package com.turkcell.rencar.presentation.screen.home

import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.UiIntent

sealed interface HomeIntent : UiIntent {
    data object ScreenStarted : HomeIntent
    data object RetryVehiclesClicked : HomeIntent
    data class LocationPermissionResult(val granted: Boolean, val canRequestAgain: Boolean) : HomeIntent
    data class MyLocationChanged(val location: LatLng) : HomeIntent
    data object RequestLocationPermissionClicked : HomeIntent
    data class CategorySelected(val category: VehicleType?) : HomeIntent
    data object FindNearestClicked : HomeIntent
    data class NavItemSelected(val item: BottomNavItem) : HomeIntent
    data object RefreshMapClicked : HomeIntent
}
