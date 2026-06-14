package com.vaultary.app.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vaultary.app.presentation.auth.AuthViewModel
import com.vaultary.app.presentation.ui.screens.DashboardPlaceholderScreen
import com.vaultary.app.presentation.ui.screens.LoginScreen
import com.vaultary.app.presentation.ui.screens.SplashScreen
import com.vaultary.app.presentation.ui.screens.TwoFactorScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val TWO_FACTOR = "two_factor"
    const val DASHBOARD = "dashboard"
}

@Composable
fun VaultaryNavGraph(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToTwoFactor = {
                    navController.navigate(Routes.TWO_FACTOR)
                }
            )
        }

        composable(Routes.TWO_FACTOR) {
            TwoFactorScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardPlaceholderScreen()
        }
    }
}
