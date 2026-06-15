package com.vaultary.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultary.app.data.remote.ProfileResponse
import com.vaultary.app.data.remote.UpdateProfileRequest
import com.vaultary.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: ProfileResponse) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(private val repository: DashboardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<String?>(null)
    val updateState: StateFlow<String?> = _updateState.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            repository.getProfile()
                .onSuccess { profile ->
                    _uiState.value = ProfileUiState.Success(profile)
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Error(error.message ?: "Failed to load profile")
                }
        }
    }

    fun updateProfile(username: String, email: String, phone: String, dob: String) {
        viewModelScope.launch {
            _updateState.value = "Updating..."
            val request = UpdateProfileRequest(
                username = username,
                email = email,
                phone = phone.ifBlank { null },
                dob = dob.ifBlank { null }
            )
            repository.updateProfile(request)
                .onSuccess {
                    _updateState.value = "Profile updated successfully"
                    fetchProfile()
                }
                .onFailure { error ->
                    _updateState.value = "Error: ${error.message}"
                }
        }
    }

    fun disable2FA() {
        viewModelScope.launch {
            _updateState.value = "Disabling 2FA..."
            repository.disable2FA()
                .onSuccess {
                    _updateState.value = "2FA disabled successfully"
                    fetchProfile()
                }
                .onFailure { error ->
                    _updateState.value = "Error: ${error.message}"
                }
        }
    }

    fun clearUpdateMessage() {
        _updateState.value = null
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProfile()
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    _updateState.value = "Failed to delete account: ${error.message}"
                }
        }
    }

    fun contactSupport(name: String, email: String, message: String) {
        viewModelScope.launch {
            _updateState.value = "Sending message..."
            val request = com.vaultary.app.data.remote.ContactRequest(name, email, message)
            repository.contactSupport(request)
                .onSuccess {
                    _updateState.value = "Support message sent successfully"
                }
                .onFailure { error ->
                    _updateState.value = "Failed to send message: ${error.message}"
                }
        }
    }

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
