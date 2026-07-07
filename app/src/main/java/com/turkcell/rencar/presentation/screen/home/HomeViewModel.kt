package com.turkcell.rencar.presentation.screen.home

import com.turkcell.rencar.presentation.component.navigation.BottomNavItem
import com.turkcell.rencar.presentation.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() :
    MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LocationPermissionResult ->
                setState { copy(permissionDenied = intent.granted.not()) }

            is HomeIntent.MyLocationChanged ->
                setState { copy(myLocation = intent.location) }

            // Route tarafından yakalanıp izin isteme launcher'ına çevrilir (bkz. LicenseUploadRoute örüntüsü).
            HomeIntent.RequestLocationPermissionClicked -> Unit

            is HomeIntent.CategorySelected ->
                setState { copy(selectedCategory = intent.category) }

            // Gerçek arama/filtreleme mantığı backend entegrasyonu ile birlikte eklenecektir.
            HomeIntent.FindNearestClicked -> Unit

            is HomeIntent.NavItemSelected -> {
                if (intent.item == BottomNavItem.Profile) {
                    sendEffect { HomeEffect.NavigateToProfile }
                } else {
                    setState { copy(selectedNavItem = intent.item) }
                }
            }
        }
    }
}
