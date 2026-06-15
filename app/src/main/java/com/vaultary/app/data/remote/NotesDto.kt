package com.vaultary.app.data.remote

data class NoteItemResponse(
    val id: Int,
    val title: String,
    val content: String,
    val created_at: String?,
    val updated_at: String?
)

data class AddNoteRequest(
    val title: String,
    val content: String
)

data class UpdateNoteRequest(
    val title: String,
    val content: String
)

data class DecryptNoteResponse(
    val status: String,
    val decrypted_title: String?,
    val decrypted_content: String?,
    val message: String?
)
