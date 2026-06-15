package com.vaultary.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultary.app.data.remote.VaultItemResponse
import com.vaultary.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VaultUiState {
    object Loading : VaultUiState()
    data class Success(val vaults: List<VaultItemResponse>) : VaultUiState()
    data class Error(val message: String) : VaultUiState()
}

class VaultViewModel(private val repository: DashboardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Loading)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val _decryptedPasswords = MutableStateFlow<Map<Int, String>>(emptyMap())
    val decryptedPasswords: StateFlow<Map<Int, String>> = _decryptedPasswords.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        fetchVaults()
    }

    fun fetchVaults() {
        _uiState.value = VaultUiState.Loading
        viewModelScope.launch {
            repository.getVaults()
                .onSuccess { vaults ->
                    _uiState.value = VaultUiState.Success(vaults)
                }
                .onFailure { error ->
                    _uiState.value = VaultUiState.Error(error.message ?: "Failed to fetch vault items")
                }
        }
    }

    fun decryptPassword(itemId: Int) {
        viewModelScope.launch {
            repository.decryptVaultItem(itemId)
                .onSuccess { response ->
                    val currentMap = _decryptedPasswords.value.toMutableMap()
                    currentMap[itemId] = response.password
                    _decryptedPasswords.value = currentMap
                }
                .onFailure { error ->
                    _actionMessage.value = "Failed to decrypt: ${error.message}"
                }
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            repository.deleteVaultItem(itemId)
                .onSuccess {
                    _actionMessage.value = "Item deleted successfully"
                    fetchVaults()
                }
                .onFailure { error ->
                    _actionMessage.value = "Failed to delete: ${error.message}"
                }
        }
    }

    fun addVault(siteName: String, siteUrl: String?, siteUsername: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            val request = com.vaultary.app.data.remote.AddVaultRequest(siteName, siteUrl, siteUsername, pass)
            repository.addVault(request)
                .onSuccess {
                    _actionMessage.value = "Vault item added"
                    fetchVaults()
                    onSuccess()
                }
                .onFailure { error ->
                    _actionMessage.value = "Failed to add: ${error.message}"
                    fetchVaults() // restore previous list
                }
        }
    }

    fun updateVault(itemId: Int, siteName: String, siteUrl: String?, siteUsername: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            val request = com.vaultary.app.data.remote.UpdateVaultRequest(siteName, siteUrl, siteUsername, pass)
            repository.updateVaultItem(itemId, request)
                .onSuccess {
                    _actionMessage.value = "Vault item updated"
                    fetchVaults()
                    onSuccess()
                }
                .onFailure { error ->
                    _actionMessage.value = "Failed to update: ${error.message}"
                    fetchVaults()
                }
        }
    }

    fun exportVault(context: android.content.Context) {
        viewModelScope.launch {
            _actionMessage.value = "Exporting..."
            repository.exportVault()
                .onSuccess { body ->
                    try {
                        val fileName = "Vaultary_Export_${System.currentTimeMillis()}.csv"
                        val contentValues = android.content.ContentValues().apply {
                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = context.contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(body.bytes())
                            }
                            _actionMessage.value = "Saved to Downloads folder!"
                        } else {
                            _actionMessage.value = "Failed to create file"
                        }
                    } catch (e: Exception) {
                        _actionMessage.value = "Error saving file: ${e.message}"
                    }
                }
                .onFailure { error ->
                    _actionMessage.value = "Export failed: ${error.message}"
                }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VaultViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
