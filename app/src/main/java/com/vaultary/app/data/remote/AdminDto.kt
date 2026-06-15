package com.vaultary.app.data.remote




data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val auth_provider: String,
    val is_admin: Boolean
)
