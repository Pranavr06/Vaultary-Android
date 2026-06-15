package com.vaultary.app.data.remote




data class ProfileResponse(
    val username: String,
    val email: String,
    val phone: String?,
    val dob: String?,
    val profile_pic: String?,
    val is_admin: Boolean,
    val is_2fa_enabled: Boolean
)


data class UpdateProfileRequest(
    val username: String,
    val email: String,
    val phone: String?,
    val dob: String?
)
