package com.turkcell.rencar.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.rencar.presentation.screen.auth.license.LicenseUploadRoute
import com.turkcell.rencar.presentation.screen.auth.login.LoginRoute
import com.turkcell.rencar.presentation.screen.auth.otp.OtpRoute
import com.turkcell.rencar.presentation.screen.auth.register.RegisterRoute
import com.turkcell.rencar.presentation.screen.cardetail.CarDetailRoute
import com.turkcell.rencar.presentation.screen.history.HistoryRoute
import com.turkcell.rencar.presentation.screen.home.HomeRoute
import com.turkcell.rencar.presentation.screen.payment.PaymentRoute
import com.turkcell.rencar.presentation.screen.profile.ProfileRoute
import com.turkcell.rencar.presentation.screen.rental.active.ActiveRentalRoute
import com.turkcell.rencar.presentation.screen.rental.photo.RentalPhotoUploadMode
import com.turkcell.rencar.presentation.screen.rental.photo.RentalPhotoUploadRoute
import com.turkcell.rencar.presentation.screen.reservation.confirmation.ReservationConfirmationRoute
import com.turkcell.rencar.presentation.screen.splash.SplashRoute
import com.turkcell.rencar.presentation.screen.wallet.WalletRoute

@Composable
fun RenCarNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = RenCarDestination.Splash.route
    ) {
        composable(RenCarDestination.Splash.route) {
            SplashRoute(
                onNavigateToLogin = {
                    navController.navigate(RenCarDestination.Login.route)
                }
            )
        }

        composable(RenCarDestination.Login.route) {
            LoginRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOtp = { phoneNumber ->
                    navController.navigate(RenCarDestination.Otp.createRoute(phoneNumber))
                },
                onNavigateToRegister = {
                    navController.navigate(RenCarDestination.Register.route)
                }
            )
        }

        composable(RenCarDestination.Register.route) {
            RegisterRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOtp = { phoneNumber ->
                    navController.navigate(RenCarDestination.Otp.createRoute(phoneNumber))
                }
            )
        }

        composable(
            route = RenCarDestination.Otp.route,
            arguments = listOf(
                navArgument(RenCarDestination.ARG_PHONE_NUMBER) { type = NavType.StringType }
            )
        ) {
            OtpRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLicenseVerification = {
                    navController.navigateAfterAuth(RenCarDestination.LicenseUpload.route)
                },
                onNavigateToHome = {
                    navController.navigateAfterAuth(RenCarDestination.Home.route)
                }
            )
        }

        composable(RenCarDestination.LicenseUpload.route) {
            LicenseUploadRoute(
                onNavigateBack = {
                    navController.navigateAfterAuth(RenCarDestination.Login.route)
                },
                onNavigateHome = {
                    navController.navigateAfterAuth(RenCarDestination.Home.route)
                },
                onNavigateToLogin = {
                    navController.navigateAfterAuth(RenCarDestination.Login.route)
                }
            )
        }

        composable(RenCarDestination.Home.route) {
            HomeRoute(
                onNavigateToProfile = {
                    navController.navigate(RenCarDestination.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToCarDetail = { vehicleId, latitude, longitude ->
                    navController.navigate(
                        RenCarDestination.CarDetail.createRoute(vehicleId, latitude, longitude)
                    ) {
                        launchSingleTop = true
                    }
                },
                onNavigateToActiveReservationCarDetail = { vehicleId ->
                    navController.navigate(
                        RenCarDestination.CarDetail.createRoute(vehicleId, latitude = null, longitude = null)
                    ) {
                        popUpTo(RenCarDestination.Home.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToActiveRentalPhotoUpload = { rentalId, vehicleId ->
                    navController.navigate(
                        RenCarDestination.RentalPhotoUpload.createRoute(
                            rentalId = rentalId,
                            vehicleId = vehicleId,
                            mode = RentalPhotoUploadMode.START_TRIP.name
                        )
                    ) {
                        popUpTo(RenCarDestination.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToActiveRentalScreen = { rentalId, vehicleId ->
                    navController.navigate(
                        RenCarDestination.ActiveRental.createRoute(rentalId, vehicleId)
                    ) {
                        popUpTo(RenCarDestination.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(RenCarDestination.History.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToWallet = {
                    navController.navigate(RenCarDestination.Wallet.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RenCarDestination.History.route) {
            HistoryRoute(
                onNavigateToMap = {
                    navController.popBackStack(RenCarDestination.Home.route, inclusive = false)
                },
                onNavigateToProfile = {
                    navController.navigate(RenCarDestination.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToWallet = {
                    navController.navigate(RenCarDestination.Wallet.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RenCarDestination.CarDetail.route,
            arguments = listOf(
                navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                navArgument(RenCarDestination.ARG_MY_LATITUDE) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(RenCarDestination.ARG_MY_LONGITUDE) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            CarDetailRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReservationConfirmation = { vehicleId ->
                    navController.navigate(
                        RenCarDestination.ReservationConfirmation.createRoute(vehicleId)
                    ) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRentalPhotoUpload = { rentalId, vehicleId ->
                    navController.navigate(
                        RenCarDestination.RentalPhotoUpload.createRoute(
                            rentalId = rentalId,
                            vehicleId = vehicleId,
                            mode = RentalPhotoUploadMode.START_TRIP.name
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                onNavigateToActiveRental = { rentalId, vehicleId ->
                    navController.navigate(
                        RenCarDestination.ActiveRental.createRoute(rentalId, vehicleId)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RenCarDestination.ReservationConfirmation.route,
            arguments = listOf(
                navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments
                ?.getString(RenCarDestination.ARG_VEHICLE_ID)
                .orEmpty()

            ReservationConfirmationRoute(
                vehicleId = vehicleId,
                onNavigateBack = { navController.popBackStack() },
                onReservationCreated = { rentalId, resultVehicleId, isPreparing ->
                    val destinationRoute = if (isPreparing) {
                        RenCarDestination.RentalPhotoUpload.createRoute(
                            rentalId = rentalId,
                            vehicleId = resultVehicleId,
                            mode = RentalPhotoUploadMode.START_TRIP.name
                        )
                    } else {
                        RenCarDestination.ActiveRental.createRoute(
                            rentalId = rentalId,
                            vehicleId = resultVehicleId
                        )
                    }
                    navController.navigate(destinationRoute) {
                        popUpTo(RenCarDestination.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RenCarDestination.RentalPhotoUpload.route,
            arguments = listOf(
                navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType },
                navArgument(RenCarDestination.ARG_PHOTO_MODE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rentalId = backStackEntry.arguments
                ?.getString(RenCarDestination.ARG_RENTAL_ID)
                .orEmpty()
            val vehicleId = backStackEntry.arguments
                ?.getString(RenCarDestination.ARG_VEHICLE_ID)
                .orEmpty()
            val mode = backStackEntry.arguments
                ?.getString(RenCarDestination.ARG_PHOTO_MODE)
                ?.let { runCatching { RentalPhotoUploadMode.valueOf(it) }.getOrNull() }
                ?: RentalPhotoUploadMode.START_TRIP

            RentalPhotoUploadRoute(
                rentalId = rentalId,
                vehicleId = vehicleId,
                mode = mode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToActiveRental = { activeRentalId, activeVehicleId ->
                    navController.navigate(
                        RenCarDestination.ActiveRental.createRoute(activeRentalId, activeVehicleId)
                    ) {
                        popUpTo(RenCarDestination.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToPayment = { paymentRentalId ->
                    navController.navigate(
                        RenCarDestination.Payment.createRoute(paymentRentalId)
                    ) {
                        popUpTo(RenCarDestination.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RenCarDestination.ActiveRental.route,
            arguments = listOf(
                navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType },
                navArgument(RenCarDestination.ARG_VEHICLE_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rentalId = backStackEntry.arguments
                ?.getString(RenCarDestination.ARG_RENTAL_ID)
                .orEmpty()
            val vehicleId = backStackEntry.arguments
                ?.getString(RenCarDestination.ARG_VEHICLE_ID)
                .orEmpty()

            ActiveRentalRoute(
                rentalId = rentalId,
                vehicleId = vehicleId,
                onNavigateToFinishPhotoUpload = { finishRentalId, finishVehicleId ->
                    navController.navigate(
                        RenCarDestination.RentalPhotoUpload.createRoute(
                            rentalId = finishRentalId,
                            vehicleId = finishVehicleId,
                            mode = RentalPhotoUploadMode.RETURN_TRIP.name
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(RenCarDestination.Home.route) {
                        popUpTo(RenCarDestination.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RenCarDestination.Profile.route) {
            ProfileRoute(
                onNavigateToHome = {
                    navController.navigate(RenCarDestination.Home.route) {
                        popUpTo(RenCarDestination.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(RenCarDestination.History.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(RenCarDestination.Login.route) {
                        popUpTo(RenCarDestination.Home.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToWallet = {
                    navController.navigate(RenCarDestination.Wallet.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RenCarDestination.Payment.route,
            arguments = listOf(
                navArgument(RenCarDestination.ARG_RENTAL_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rentalId = backStackEntry.arguments
                ?.getString(RenCarDestination.ARG_RENTAL_ID)
                .orEmpty()

            PaymentRoute(
                rentalId = rentalId,
                onNavigateHome = {
                    navController.navigate(RenCarDestination.Home.route) {
                        popUpTo(RenCarDestination.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToWallet = {
                    navController.navigate(RenCarDestination.Wallet.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RenCarDestination.Wallet.route) {
            WalletRoute(
                onNavigateToMap = {
                    navController.popBackStack(RenCarDestination.Home.route, inclusive = false)
                },
                onNavigateToHistory = {
                    navController.navigate(RenCarDestination.History.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(RenCarDestination.Profile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

private fun NavHostController.navigateAfterAuth(route: String) {
    navigate(route) {
        popUpTo(RenCarDestination.Splash.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
