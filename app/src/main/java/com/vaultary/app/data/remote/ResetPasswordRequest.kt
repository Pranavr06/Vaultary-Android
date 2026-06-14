package com.vaultary.app.data.remote

data class ResetPasswordRequest(
    val token: String,
    val password: String
)
