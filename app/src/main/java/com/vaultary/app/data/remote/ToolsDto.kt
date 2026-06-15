package com.vaultary.app.data.remote




data class CheckPasswordRequest(
    val password: String
)


data class CheckPasswordResponse(
    val score: Int,
    val crack_time: String,
    val feedback: Map<String, Any>?,
    val breach_count: Int,
    val guesses: Double,
    val sequence: List<Map<String, Any>>?,
    val password_length: Int
)


data class HistoryResponse(
    val score: Int,
    val date: String
)


data class GeneratePasswordResponse(
    val password: String
)


data class ContactRequest(
    val name: String,
    val email: String,
    val message: String
)
