package com.vaultary.app.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultary.app.data.remote.AddNoteRequest
import com.vaultary.app.data.remote.NoteItemResponse
import com.vaultary.app.data.remote.UpdateNoteRequest
import com.vaultary.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotesUiState {
    object Loading : NotesUiState()
    data class Success(val notes: List<NoteItemResponse>) : NotesUiState()
    data class Error(val message: String) : NotesUiState()
}

class NotesViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _decryptedNotes = MutableStateFlow<Map<Int, Pair<String, String>>>(emptyMap())
    val decryptedNotes: StateFlow<Map<Int, Pair<String, String>>> = _decryptedNotes.asStateFlow()

    fun fetchNotes() {
        _uiState.value = NotesUiState.Loading
        viewModelScope.launch {
            val result = repository.getNotes()
            result.onSuccess { notes ->
                _uiState.value = NotesUiState.Success(notes)
            }.onFailure { error ->
                _uiState.value = NotesUiState.Error(error.message ?: "Failed to fetch notes")
            }
        }
    }

    fun addNote(title: String, content: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.addNote(AddNoteRequest(title, content))
            result.onSuccess {
                fetchNotes()
                onSuccess()
            }.onFailure { error ->
                _uiState.value = NotesUiState.Error(error.message ?: "Failed to add note")
            }
        }
    }

    fun updateNote(id: Int, title: String, content: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.updateNote(id, UpdateNoteRequest(title, content))
            result.onSuccess {
                fetchNotes()
                onSuccess()
            }.onFailure { error ->
                _uiState.value = NotesUiState.Error(error.message ?: "Failed to update note")
            }
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            val result = repository.deleteNote(id)
            result.onSuccess {
                fetchNotes()
            }.onFailure { error ->
                _uiState.value = NotesUiState.Error(error.message ?: "Failed to delete note")
            }
        }
    }

    fun decryptNote(id: Int) {
        viewModelScope.launch {
            val result = repository.decryptNote(id)
            result.onSuccess { response ->
                val currentMap = _decryptedNotes.value.toMutableMap()
                currentMap[id] = Pair(response.decrypted_title ?: "", response.decrypted_content ?: "")
                _decryptedNotes.value = currentMap
            }.onFailure {
                // Handle decrypt failure
            }
        }
    }

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
