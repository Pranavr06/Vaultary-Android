package com.vaultary.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vaultary.app.presentation.auth.AuthUiState
import com.vaultary.app.presentation.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500) // Simulate splash delay
        viewModel.checkInitialAuth()
    }

    val state = viewModel.uiState.value
    LaunchedEffect(state) {
        when (state) {
            is AuthUiState.Success -> onNavigateToDashboard()
            is AuthUiState.Idle -> {
                // If it's idle after check, it means no token was found
                onNavigateToLogin()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Vaultary",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
