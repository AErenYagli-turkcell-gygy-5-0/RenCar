package com.turkcell.rencar.presentation.navigation

import android.net.Uri

sealed class RenCarDestination(val route: String) {

    data object Splash : RenCarDestination("splash")

    data object Login : RenCarDestination("login")

    data object Otp : RenCarDestination("otp/{$ARG_PHONE_NUMBER}") {
        fun createRoute(phoneNumber: String) = "otp/${Uri.encode(phoneNumber)}"
    }

    companion object {
        const val ARG_PHONE_NUMBER = "phoneNumber"
    }
}
