package com.vaultary.app.presentation.ui.navigation

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vaultary.app.presentation.auth.AuthViewModel
import com.vaultary.app.presentation.ui.screens.*

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val TWO_FACTOR = "two_factor"
    const val TWO_FACTOR_SETUP = "two_factor_setup"
    const val DASHBOARD = "dashboard"
    const val ADD_EDIT_VAULT = "add_edit_vault"
    const val ADD_EDIT_NOTE = "add_edit_note"

    fun createAddEditVaultRoute(itemId: Int?): String {
        return if (itemId == null) "add_edit_vault" else "add_edit_vault?itemId=$itemId"
    }

    fun createAddEditNoteRoute(itemId: Int?): String {
        return if (itemId == null) "add_edit_note" else "add_edit_note?itemId=$itemId"
    }
}

@Composable
fun VaultaryNavGraph(
    authViewModel: AuthViewModel,
    dashboardRepository: com.vaultary.app.data.repository.DashboardRepository,
    onLogoutComplete: () -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                viewModel = authViewModel,
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
            val context = LocalContext.current
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToTwoFactor = {
                    navController.navigate(Routes.TWO_FACTOR)
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onSocialLoginClick = { provider ->
                    val url = "https://vaultary.vercel.app/login/$provider"
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.RESET_PASSWORD) {
            ResetPasswordScreen(
                viewModel = authViewModel,
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.TWO_FACTOR) {
            TwoFactorScreen(
                viewModel = authViewModel,
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.TWO_FACTOR_SETUP) {
            val twoFactorSetupViewModel: com.vaultary.app.presentation.ui.screens.TwoFactorSetupViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.vaultary.app.presentation.ui.screens.TwoFactorSetupViewModel.Factory(dashboardRepository)
            )
            com.vaultary.app.presentation.ui.screens.TwoFactorSetupScreen(
                viewModel = twoFactorSetupViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSetupSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Routes.ADD_EDIT_VAULT}?itemId={itemId}",
            arguments = listOf(androidx.navigation.navArgument("itemId") { 
                type = androidx.navigation.NavType.IntType 
                defaultValue = -1
            })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId")?.takeIf { it != -1 }
            val vaultViewModel: com.vaultary.app.presentation.dashboard.VaultViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.vaultary.app.presentation.dashboard.VaultViewModel.Factory(dashboardRepository)
            )
            com.vaultary.app.presentation.ui.screens.AddEditVaultScreen(
                itemId = itemId,
                viewModel = vaultViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.ADD_EDIT_NOTE}?itemId={itemId}",
            arguments = listOf(androidx.navigation.navArgument("itemId") { 
                type = androidx.navigation.NavType.IntType 
                defaultValue = -1
            })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId")?.takeIf { it != -1 }
            val notesViewModel: com.vaultary.app.presentation.ui.screens.NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.vaultary.app.presentation.ui.screens.NotesViewModel.Factory(dashboardRepository)
            )
            com.vaultary.app.presentation.ui.screens.AddEditNoteScreen(
                itemId = itemId,
                viewModel = notesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            val isAdmin = (authViewModel.uiState.collectAsState().value as? com.vaultary.app.presentation.auth.AuthUiState.Success)?.isAdmin == true
            DashboardScreen(
                isAdmin = isAdmin,
                dashboardRepository = dashboardRepository,
                onLogout = {
                    onLogoutComplete()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateTo2FASetup = {
                    navController.navigate(Routes.TWO_FACTOR_SETUP)
                },
                onNavigateToAddEditVault = { itemId ->
                    if (itemId == null) {
                        navController.navigate(Routes.ADD_EDIT_VAULT)
                    } else {
                        navController.navigate("${Routes.ADD_EDIT_VAULT}?itemId=$itemId")
                    }
                },
                onNavigateToAddEditNote = { itemId ->
                    if (itemId == null) {
                        navController.navigate(Routes.ADD_EDIT_NOTE)
                    } else {
                        navController.navigate("${Routes.ADD_EDIT_NOTE}?itemId=$itemId")
                    }
                },
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
    }
}
