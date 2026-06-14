package com.vaultary.app.data.remote

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String,
    val dob: String
)
