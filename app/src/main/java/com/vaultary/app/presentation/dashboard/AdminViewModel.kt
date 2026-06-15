package com.vaultary.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultary.app.data.remote.UserResponse
import com.vaultary.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminUiState {
    object Loading : AdminUiState()
    data class Success(val users: List<UserResponse>) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

class AdminViewModel(private val repository: DashboardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            repository.getAllUsers()
                .onSuccess { users ->
                    _uiState.value = AdminUiState.Success(users)
                }
                .onFailure { error ->
                    _uiState.value = AdminUiState.Error(error.message ?: "Failed to fetch users")
                }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            repository.adminDeleteUser(userId)
                .onSuccess {
                    _actionMessage.value = "User deleted successfully"
                    fetchUsers()
                }
                .onFailure { error ->
                    _actionMessage.value = "Failed to delete user: ${error.message}"
                }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
