package com.vaultary.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultary.app.presentation.dashboard.VaultUiState
import com.vaultary.app.presentation.dashboard.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVaultScreen(
    itemId: Int?,
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val decryptedPasswords by viewModel.decryptedPasswords.collectAsState()

    var siteName by remember { mutableStateOf("") }
    var siteUrl by remember { mutableStateOf("") }
    var siteUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isInitialized by remember { mutableStateOf(false) }

    // Pre-fill if editing
    LaunchedEffect(itemId, uiState) {
        if (!isInitialized && itemId != null && uiState is VaultUiState.Success) {
            val item = (uiState as VaultUiState.Success).vaults.find { it.id == itemId }
            if (item != null) {
                siteName = item.site_name
                siteUrl = item.site_url ?: ""
                siteUsername = item.site_username
                password = decryptedPasswords[itemId] ?: ""
                
                // Trigger decryption if password not yet loaded
                if (decryptedPasswords[itemId] == null) {
                    viewModel.decryptPassword(itemId)
                }
                isInitialized = true
            }
        }
    }

    // Update password state when decryption finishes
    LaunchedEffect(decryptedPasswords) {
        if (itemId != null && decryptedPasswords[itemId] != null && password.isEmpty()) {
            password = decryptedPasswords[itemId] ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Add Vault Item" else "Edit Vault Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = siteName,
                onValueChange = { siteName = it },
                label = { Text("Site Name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = siteUrl,
                onValueChange = { siteUrl = it },
                label = { Text("Site URL (Optional)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = siteUsername,
                onValueChange = { siteUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    if (itemId == null) {
                        viewModel.addVault(siteName, siteUrl, siteUsername, password) {
                            onNavigateBack()
                        }
                    } else {
                        viewModel.updateVault(itemId, siteName, siteUrl, siteUsername, password) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", fontSize = 16.sp)
            }
        }
    }
}
