package com.turkcell.rencar.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.rencar.presentation.screen.auth.login.LoginRoute
import com.turkcell.rencar.presentation.screen.auth.otp.OtpRoute
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
                // Doğrulama sonrası gidilecek bir Home/Dashboard ekranı henüz kapsamda değil
                // (bkz. docs/decisions.md — 2026-07-02, Sunum Katmanı MVI kararı).
                onVerified = {}
            )
        }
    }
}
