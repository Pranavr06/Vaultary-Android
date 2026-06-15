package com.vaultary.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultary.app.data.remote.CheckPasswordRequest
import com.vaultary.app.data.remote.CheckPasswordResponse
import com.vaultary.app.data.remote.HistoryResponse
import com.vaultary.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToolsViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _passwordCheckResult = MutableStateFlow<CheckPasswordResponse?>(null)
    val passwordCheckResult: StateFlow<CheckPasswordResponse?> = _passwordCheckResult.asStateFlow()

    private val _generatedPassword = MutableStateFlow<String?>(null)
    val generatedPassword: StateFlow<String?> = _generatedPassword.asStateFlow()

    private val _historyList = MutableStateFlow<List<HistoryResponse>>(emptyList())
    val historyList: StateFlow<List<HistoryResponse>> = _historyList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun checkPassword(password: String) {
        if (password.isBlank()) {
            _passwordCheckResult.value = null
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.checkPassword(CheckPasswordRequest(password))
                .onSuccess {
                    _passwordCheckResult.value = it
                    fetchHistory() // Refresh history after checking
                }
                .onFailure { e ->
                    _error.value = "Failed to check password: ${e.message}"
                }
            _isLoading.value = false
        }
    }

    fun generatePassword() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.generatePassword()
                .onSuccess {
                    _generatedPassword.value = it.password
                }
                .onFailure { e ->
                    _error.value = "Failed to generate password: ${e.message}"
                }
            _isLoading.value = false
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            repository.getHistory()
                .onSuccess {
                    _historyList.value = it
                }
                .onFailure { e ->
                    _error.value = "Failed to fetch history: ${e.message}"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ToolsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ToolsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
