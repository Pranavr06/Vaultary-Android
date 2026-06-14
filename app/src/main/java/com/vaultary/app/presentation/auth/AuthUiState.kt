package com.vaultary.app.presentation.auth

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val isAdmin: Boolean? = false) : AuthUiState()
    object TwoFactorRequired : AuthUiState()
    object RegistrationSuccess : AuthUiState()
    data class ResetEmailSent(val message: String) : AuthUiState()
    data class ResetSuccess(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
