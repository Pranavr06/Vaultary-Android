package com.vaultary.app.presentation.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultary.app.data.remote.TwoFactorEnableRequest
import com.vaultary.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TwoFactorSetupUiState {
    object Idle : TwoFactorSetupUiState()
    object Loading : TwoFactorSetupUiState()
    data class SetupInfo(val secret: String, val qrBase64: String) : TwoFactorSetupUiState()
    object Success : TwoFactorSetupUiState()
    data class Error(val message: String) : TwoFactorSetupUiState()
}

class TwoFactorSetupViewModel(private val repository: DashboardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<TwoFactorSetupUiState>(TwoFactorSetupUiState.Idle)
    val uiState: StateFlow<TwoFactorSetupUiState> = _uiState.asStateFlow()

    fun fetchSetupInfo() {
        _uiState.value = TwoFactorSetupUiState.Loading
        viewModelScope.launch {
            repository.setup2FA()
                .onSuccess { info ->
                    // Backend returns qr_image as a base64 encoded string starting with "data:image/png;base64,"
                    val base64Image = info.qr_image.substringAfter("base64,")
                    _uiState.value = TwoFactorSetupUiState.SetupInfo(info.secret, base64Image)
                }
                .onFailure { error ->
                    _uiState.value = TwoFactorSetupUiState.Error(error.message ?: "Failed to fetch 2FA setup")
                }
        }
    }

    fun enable2FA(code: String) {
        _uiState.value = TwoFactorSetupUiState.Loading
        viewModelScope.launch {
            repository.enable2FA(TwoFactorEnableRequest(code))
                .onSuccess {
                    _uiState.value = TwoFactorSetupUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = TwoFactorSetupUiState.Error(error.message ?: "Failed to verify code")
                }
        }
    }

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TwoFactorSetupViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TwoFactorSetupViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

@Composable
fun TwoFactorSetupScreen(
    viewModel: TwoFactorSetupViewModel,
    onNavigateBack: () -> Unit,
    onSetupSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchSetupInfo()
    }

    LaunchedEffect(uiState) {
        if (uiState is TwoFactorSetupUiState.Success) {
            Toast.makeText(context, "2FA Enabled successfully", Toast.LENGTH_SHORT).show()
            onSetupSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is TwoFactorSetupUiState.Loading, TwoFactorSetupUiState.Idle -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is TwoFactorSetupUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) { Text("Go Back") }
                }
            }
            is TwoFactorSetupUiState.SetupInfo -> {
                var code by remember { mutableStateOf("") }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Setup Two-Factor Authentication",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Scan this QR code with Google Authenticator or Authy.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    val bitmap = remember(state.qrBase64) {
                        try {
                            val imageBytes = Base64.decode(state.qrBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "2FA QR Code",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(bottom = 24.dp)
                        )
                    } else {
                        Text("Failed to render QR Code", color = MaterialTheme.colorScheme.error)
                    }

                    Text(
                        text = "Or enter this secret manually:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = state.secret,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        IconButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(state.secret))
                            Toast.makeText(context, "Copied secret", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "Copy Secret",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Enter 6-digit code") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.enable2FA(code) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Verify & Enable", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is TwoFactorSetupUiState.Success -> {
                // Handled in LaunchedEffect
            }
        }
    }
}
