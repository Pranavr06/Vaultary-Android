package com.vaultary.app.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import com.vaultary.app.data.remote.VaultItemResponse
import com.vaultary.app.presentation.dashboard.VaultUiState
import com.vaultary.app.presentation.dashboard.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onNavigateToAddEdit: (Int?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val decryptedPasswords by viewModel.decryptedPasswords.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchVaults()
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Vault", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    TextButton(onClick = { viewModel.exportVault(context) }) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export as CSV", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEdit(null) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Vault Item")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is VaultUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is VaultUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is VaultUiState.Success -> {
                    if (state.vaults.isEmpty()) {
                        Text(
                            text = "Your vault is empty.\nClick + to add a new password.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        var itemToDelete by remember { mutableStateOf<Int?>(null) }
                        
                        if (itemToDelete != null) {
                            AlertDialog(
                                onDismissRequest = { itemToDelete = null },
                                title = { Text("Delete Password") },
                                text = { Text("Are you sure to delete? This is permanent and can't be reversed.") },
                                confirmButton = {
                                    Button(onClick = { 
                                        viewModel.deleteItem(itemToDelete!!)
                                        itemToDelete = null
                                    }) { Text("Delete") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
                                }
                            )
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.vaults) { item ->
                                VaultItemCard(
                                    item = item,
                                    decryptedPassword = decryptedPasswords[item.id],
                                    onDecrypt = { item.id?.let { viewModel.decryptPassword(it) } },
                                    onEdit = { item.id?.let { onNavigateToAddEdit(it) } },
                                    onDelete = { item.id?.let { itemToDelete = it } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VaultItemCard(
    item: VaultItemResponse,
    decryptedPassword: String?,
    onDecrypt: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.site_name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }

            Text(
                text = item.site_username,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                var isPasswordVisible by remember { mutableStateOf(false) }
                
                Text(
                    text = if (isPasswordVisible && decryptedPassword != null) decryptedPassword else "••••••••••••",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (decryptedPassword == null) {
                        onDecrypt()
                        isPasswordVisible = true
                    } else {
                        isPasswordVisible = !isPasswordVisible
                    }
                }) {
                    Icon(
                        if (isPasswordVisible && decryptedPassword != null) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle Visibility",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (decryptedPassword != null) {
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(decryptedPassword))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy Password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
