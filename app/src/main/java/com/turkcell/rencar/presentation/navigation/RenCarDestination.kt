package com.turkcell.rencar.presentation.navigation

import android.net.Uri

sealed class RenCarDestination(val route: String) {

    data object Splash : RenCarDestination("splash")

    data object Login : RenCarDestination("login")

    data object Register : RenCarDestination("register")

    data object Otp : RenCarDestination("otp/{$ARG_PHONE_NUMBER}") {
        fun createRoute(phoneNumber: String) = "otp/${Uri.encode(phoneNumber)}"
    }

    data object LicenseUpload : RenCarDestination("license-upload")

    data object Home : RenCarDestination("home")

    data object Profile : RenCarDestination("profile")

    data object CarDetail : RenCarDestination(
        "car-detail/{$ARG_VEHICLE_ID}?$ARG_MY_LATITUDE={$ARG_MY_LATITUDE}&$ARG_MY_LONGITUDE={$ARG_MY_LONGITUDE}"
    ) {
        fun createRoute(vehicleId: String, latitude: Double?, longitude: Double?): String {
            val encodedId = Uri.encode(vehicleId)
            return "car-detail/$encodedId?$ARG_MY_LATITUDE=${latitude ?: ""}&$ARG_MY_LONGITUDE=${longitude ?: ""}"
        }
    }

    companion object {
        const val ARG_PHONE_NUMBER = "phoneNumber"
        const val ARG_VEHICLE_ID = "vehicleId"
        const val ARG_MY_LATITUDE = "myLatitude"
        const val ARG_MY_LONGITUDE = "myLongitude"
    }
}
