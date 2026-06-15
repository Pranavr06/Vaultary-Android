package com.vaultary.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.vaultary.app.data.local.TokenManager
import com.vaultary.app.data.remote.RetrofitInstance
import com.vaultary.app.data.repository.AuthRepository
import com.vaultary.app.data.repository.DashboardRepository
import com.vaultary.app.presentation.auth.AuthViewModel
import com.vaultary.app.presentation.ui.navigation.VaultaryNavGraph
import com.vaultary.app.ui.theme.VaultaryTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vaultary.app.presentation.ui.screens.AppLockScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual Dependency Injection
        val tokenManager = TokenManager(applicationContext)
        val api = RetrofitInstance.getApi(tokenManager)
        val authRepository = AuthRepository(api, tokenManager)
        val dashboardRepository = DashboardRepository(api)
        val authFactory = AuthViewModel.Factory(authRepository)
        val authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val initialTheme = sharedPrefs.getBoolean("is_dark_theme", true) // Default to dark theme if not set

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (tokenManager.getToken() != null) {
                    authViewModel.setAppUnlocked(false)
                }
            }
        })

        setContent {
            var isDarkTheme by remember { mutableStateOf(initialTheme) }

            VaultaryTheme(darkTheme = isDarkTheme) {
                val isUnlocked by authViewModel.isAppUnlocked.collectAsState()
                val isLoggedIn = tokenManager.getToken() != null

                if (isLoggedIn && !isUnlocked) {
                    AppLockScreen(onUnlock = { authViewModel.setAppUnlocked(true) })
                } else {
                    VaultaryNavGraph(
                        authViewModel = authViewModel,
                        dashboardRepository = dashboardRepository,
                        onLogoutComplete = {
                            tokenManager.deleteToken()
                            authViewModel.setAppUnlocked(false)
                        },
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = {
                            isDarkTheme = !isDarkTheme
                            sharedPrefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply()
                        }
                    )
                }
            }
        }
    }
}