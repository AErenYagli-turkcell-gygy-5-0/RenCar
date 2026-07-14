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

    data object History : RenCarDestination("history")

    data object Profile : RenCarDestination("profile")

    data object CarDetail : RenCarDestination(
        "car-detail/{$ARG_VEHICLE_ID}?$ARG_MY_LATITUDE={$ARG_MY_LATITUDE}&$ARG_MY_LONGITUDE={$ARG_MY_LONGITUDE}"
    ) {
        fun createRoute(vehicleId: String, latitude: Double?, longitude: Double?): String {
            val encodedId = Uri.encode(vehicleId)
            return "car-detail/$encodedId?$ARG_MY_LATITUDE=${latitude ?: ""}&$ARG_MY_LONGITUDE=${longitude ?: ""}"
        }
    }

    data object ReservationConfirmation : RenCarDestination(
        "reservation-confirmation/{$ARG_VEHICLE_ID}"
    ) {
        fun createRoute(vehicleId: String): String =
            "reservation-confirmation/${Uri.encode(vehicleId)}"
    }

    data object RentalPhotoUpload : RenCarDestination(
        "rental-photo-upload/{$ARG_RENTAL_ID}/{$ARG_VEHICLE_ID}/{$ARG_PHOTO_MODE}"
    ) {
        fun createRoute(rentalId: String, vehicleId: String, mode: String): String =
            "rental-photo-upload/${Uri.encode(rentalId)}/${Uri.encode(vehicleId)}/${Uri.encode(mode)}"
    }

    data object ActiveRental : RenCarDestination(
        "active-rental/{$ARG_RENTAL_ID}/{$ARG_VEHICLE_ID}"
    ) {
        fun createRoute(rentalId: String, vehicleId: String): String =
            "active-rental/${Uri.encode(rentalId)}/${Uri.encode(vehicleId)}"
    }

    companion object {
        const val ARG_PHONE_NUMBER = "phoneNumber"
        const val ARG_VEHICLE_ID = "vehicleId"
        const val ARG_MY_LATITUDE = "myLatitude"
        const val ARG_MY_LONGITUDE = "myLongitude"
        const val ARG_RENTAL_ID = "rentalId"
        const val ARG_PHOTO_MODE = "photoMode"
    }
}
