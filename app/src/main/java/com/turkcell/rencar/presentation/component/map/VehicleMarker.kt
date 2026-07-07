package com.turkcell.rencar.presentation.component.map

import androidx.compose.ui.graphics.Color
import com.turkcell.rencar.presentation.theme.RenCarExtendedColors

// RencarMap ve HomeState arasında paylaşılan hafif konum tipi; Context/Android SDK bağımlılığı taşımaz.
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

enum class VehicleCategory {
    Economic,
    Comfort,
    Suv,
    Extra
}

data class VehicleMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val price: Int,
    val category: VehicleCategory
)

// Kategori vurgu renklerini docs/design/00-color-system.md §4.3 ile eşler (RencarMap ve HomeScreen arasında paylaşılır).
fun VehicleCategory.color(colors: RenCarExtendedColors): Color = when (this) {
    VehicleCategory.Economic -> colors.categoryEconomic
    VehicleCategory.Comfort -> colors.categoryPremium
    VehicleCategory.Suv -> colors.categorySuv
    VehicleCategory.Extra -> colors.categoryExtra
}
