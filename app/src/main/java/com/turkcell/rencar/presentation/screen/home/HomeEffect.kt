package com.turkcell.rencar.presentation.screen.home

import com.turkcell.rencar.presentation.component.map.LatLng
import com.turkcell.rencar.presentation.core.mvi.UiEffect

sealed interface HomeEffect : UiEffect {
    data object NavigateToProfile : HomeEffect
    data class NavigateToCarDetail(val vehicleId: String, val myLocation: LatLng?) : HomeEffect
}
