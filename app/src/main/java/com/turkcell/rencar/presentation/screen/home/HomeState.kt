package com.turkcell.rencar.presentation.screen.home

import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.presentation.component.map.VehicleCategory
import com.turkcell.rencar.presentation.component.map.VehicleMarker
import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.UiState

data class HomeState(
    val myLocation: LatLng? = null,
    // null: izin sonucu henüz gelmedi, true: reddedildi, false: verildi.
    val permissionDenied: Boolean? = null,
    val vehicles: List<VehicleMarker> = mockVehicles,
    val selectedCategory: VehicleCategory? = null,
    val nearbyCount: Int = 12,
    val locationLabel: String = "Kadıköy çevresinde",
    val distanceLabel: String = "3 dk uzaklıkta",
    val selectedNavItem: BottomNavItem = BottomNavItem.Map
) : UiState

// Backend entegrasyonu tamamlanana kadar kullanılan sabit örnek araç listesi (bkz. docs/decisions.md, 2026-07-07).
val mockVehicles = listOf(
    VehicleMarker(id = "1", latitude = 40.9930, longitude = 29.0270, price = 28, category = VehicleCategory.Economic),
    VehicleMarker(id = "2", latitude = 40.9945, longitude = 29.0320, price = 38, category = VehicleCategory.Comfort),
    VehicleMarker(id = "3", latitude = 40.9890, longitude = 29.0300, price = 32, category = VehicleCategory.Suv),
    VehicleMarker(id = "4", latitude = 40.9875, longitude = 29.0260, price = 26, category = VehicleCategory.Extra)
)
