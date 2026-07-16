package com.turkcell.rencar.presentation.component.map

import androidx.compose.ui.graphics.Color
import com.turkcell.rencar.domain.vehicle.VehicleType
import com.turkcell.rencar.presentation.theme.RenCarExtendedColors

// RencarMap ve HomeState arasında paylaşılan hafif konum tipi; Context/Android SDK bağımlılığı taşımaz.
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class VehicleMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val price: Int,
    val category: VehicleType,
    val isReservedByMe: Boolean = false
)

// Backend'de araç segmenti (ekonomik/konfor/extra) alanı yoktur, yalnızca gövde tipi (VehicleType)
// döner (bkz. docs/decisions.md, 2026-07-08). Marker rengi salt görsel bir ayrım olduğundan mevcut
// 4 tema tokenı 5 tipe dağıtılır; SUV, isim eşleşmesiyle kendi tokenını korur.
fun VehicleType.color(colors: RenCarExtendedColors): Color = when (this) {
    VehicleType.SEDAN -> colors.categoryEconomic
    VehicleType.HATCHBACK -> colors.categoryPremium
    VehicleType.STATION -> colors.categoryExtra
    VehicleType.MINIVAN -> colors.categoryEconomic
    VehicleType.SUV -> colors.categorySuv
}
