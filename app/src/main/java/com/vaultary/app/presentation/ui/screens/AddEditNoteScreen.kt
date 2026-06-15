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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    itemId: Int?,
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val decryptedNotes by viewModel.decryptedNotes.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Pre-fill if editing
    LaunchedEffect(itemId, uiState) {
        if (!isInitialized && itemId != null && uiState is NotesUiState.Success) {
            val item = (uiState as NotesUiState.Success).notes.find { it.id == itemId }
            if (item != null) {
                // If we already decrypted it before, prefill it
                if (decryptedNotes[itemId] != null) {
                    title = decryptedNotes[itemId]?.first ?: ""
                    content = decryptedNotes[itemId]?.second ?: ""
                    isInitialized = true
                } else {
                    viewModel.decryptNote(itemId)
                }
            }
        }
    }

    LaunchedEffect(decryptedNotes) {
        if (itemId != null && decryptedNotes[itemId] != null && !isInitialized) {
            title = decryptedNotes[itemId]?.first ?: ""
            content = decryptedNotes[itemId]?.second ?: ""
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Add Note" else "Edit Note", fontWeight = FontWeight.Bold) },
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
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Secure Content") },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 24.dp),
                singleLine = false
            )

            Button(
                onClick = {
                    if (itemId == null) {
                        viewModel.addNote(title, content) {
                            onNavigateBack()
                        }
                    } else {
                        viewModel.updateNote(itemId, title, content) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Secure Note", fontSize = 16.sp)
            }
        }
    }
}
