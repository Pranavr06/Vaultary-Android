package com.vaultary.app.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultary.app.presentation.dashboard.ProfileUiState
import com.vaultary.app.presentation.dashboard.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onNavigateTo2FASetup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(updateState) {
        updateState?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUpdateMessage()
        }
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is ProfileUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(), 
                verticalArrangement = Arrangement.Center, 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onLogout) {
                    Text("Logout & Login Again")
                }
            }
        }
        is ProfileUiState.Success -> {
            var username by remember { mutableStateOf(state.profile.username) }
            var email by remember { mutableStateOf(state.profile.email) }
            var phone by remember { mutableStateOf(state.profile.phone ?: "") }
            var dob by remember { mutableStateOf(state.profile.dob ?: "") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Account Settings",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Security Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Security", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Two-Factor Authentication", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                if (state.profile.is_2fa_enabled) {
                                    viewModel.disable2FA()
                                } else {
                                    onNavigateTo2FASetup()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.profile.is_2fa_enabled) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (state.profile.is_2fa_enabled) "Disable 2FA" else "Enable 2FA")
                        }
                    }
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    singleLine = true
                )

                Button(
                    onClick = { viewModel.updateProfile(username, email, phone, dob) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                var showContactDialog by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { showContactDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Contact Support", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Logout", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { viewModel.deleteAccount(onSuccess = onLogout) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Account", color = Color(0xFFEF4444))
                }

                if (showContactDialog) {
                    var contactName by remember { mutableStateOf(state.profile.username) }
                    var contactEmail by remember { mutableStateOf(state.profile.email) }
                    var contactMessage by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { showContactDialog = false },
                        title = { Text("Contact Support") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = contactName,
                                    onValueChange = { contactName = it },
                                    label = { Text("Name") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = contactEmail,
                                    onValueChange = { contactEmail = it },
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = contactMessage,
                                    onValueChange = { contactMessage = it },
                                    label = { Text("Message") },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    maxLines = 4
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.contactSupport(contactName, contactEmail, contactMessage)
                                showContactDialog = false
                            }) {
                                Text("Send")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showContactDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}
