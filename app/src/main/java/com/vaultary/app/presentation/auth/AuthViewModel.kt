package com.vaultary.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultary.app.data.remote.LoginRequest
import com.vaultary.app.data.remote.Verify2faRequest
import com.vaultary.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var tempToken: String? = null

    fun checkInitialAuth() {
        if (repository.isLoggedIn()) {
            _uiState.value = AuthUiState.Success(isAdmin = false)
        }
    }

    fun login(username: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.login(LoginRequest(username, password))
            result.onSuccess { response ->
                when (response.status) {
                    "success" -> {
                        _uiState.value = AuthUiState.Success(isAdmin = response.isAdmin)
                    }
                    "2fa_required" -> {
                        tempToken = response.tempToken
                        _uiState.value = AuthUiState.TwoFactorRequired
                    }
                    else -> {
                        _uiState.value = AuthUiState.Error(response.message ?: "Unknown error")
                    }
                }
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Login failed")
            }
        }
    }

    fun verify2fa(code: String) {
        val currentTempToken = tempToken
        if (currentTempToken == null) {
            _uiState.value = AuthUiState.Error("Session expired. Please login again.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.verify2fa(Verify2faRequest(currentTempToken, code))
            result.onSuccess { response ->
                if (response.status == "success") {
                    tempToken = null
                    _uiState.value = AuthUiState.Success(isAdmin = response.isAdmin)
                } else {
                    _uiState.value = AuthUiState.Error(response.message ?: "Invalid code")
                }
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Verification failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
