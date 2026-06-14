package com.vaultary.app.presentation.auth

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val isAdmin: Boolean? = false) : AuthUiState()
    object TwoFactorRequired : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
