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
import com.turkcell.rencar.presentation.screen.home.HomeRoute
import com.turkcell.rencar.presentation.screen.splash.SplashRoute

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
            HomeRoute()
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
