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

    fun register(username: String, email: String, password: String, phone: String, dob: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Email is required for password recovery.")
            return
        }
        if (!username.matches("^[a-zA-Z0-9_]*$".toRegex())) {
            _uiState.value = AuthUiState.Error("Username can only contain letters, numbers, and underscores.")
            return
        }
        if (phone.isNotEmpty() && !phone.matches("^\\d{10}$".toRegex())) {
            _uiState.value = AuthUiState.Error("Phone number must be exactly 10 digits.")
            return
        }
        if (dob.isEmpty()) {
            _uiState.value = AuthUiState.Error("Date of Birth is required.")
            return
        }
        try {
            val parts = dob.split("/")
            if (parts.size != 3) throw Exception("Invalid format")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            val today = java.time.LocalDate.now()
            val birthDate = java.time.LocalDate.of(year, month, day)
            var age = today.year - birthDate.year
            if (today.monthValue < birthDate.monthValue || (today.monthValue == birthDate.monthValue && today.dayOfMonth < birthDate.dayOfMonth)) {
                age--
            }

            if (birthDate.isAfter(today)) {
                _uiState.value = AuthUiState.Error("Date of birth cannot be in the future.")
                return
            }
            if (age < 13) {
                _uiState.value = AuthUiState.Error("You must be at least 13 years old to register.")
                return
            }
            if (age > 120) {
                _uiState.value = AuthUiState.Error("Please enter a valid date of birth.")
                return
            }
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Invalid Date of Birth format (DD/MM/YYYY).")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val request = com.vaultary.app.data.remote.RegisterRequest(username, email, password, phone, dob)
            val result = repository.register(request)
            result.onSuccess {
                _uiState.value = AuthUiState.RegistrationSuccess
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Email is required.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val request = com.vaultary.app.data.remote.ForgotPasswordRequest(email)
            val result = repository.forgotPassword(request)
            result.onSuccess { response ->
                _uiState.value = AuthUiState.ResetEmailSent(response.message ?: "Link sent if exists.")
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Request failed")
            }
        }
    }

    fun resetPasswordConfirm(token: String, password: String) {
        if (token.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Token and password are required.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val request = com.vaultary.app.data.remote.ResetPasswordRequest(token, password)
            val result = repository.resetPasswordConfirm(request)
            result.onSuccess { response ->
                _uiState.value = AuthUiState.ResetSuccess(response.message ?: "Success!")
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Reset failed")
            }
        }
    }

    fun handleSocialLoginToken(token: String) {
        repository.saveSocialLoginToken(token)
        _uiState.value = AuthUiState.Success(isAdmin = false)
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
