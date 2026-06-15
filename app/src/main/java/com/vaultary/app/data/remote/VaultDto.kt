package com.vaultary.app.data.remote




data class VaultItemResponse(
    val id: Int? = null,
    val site_name: String,
    val site_url: String?,
    val site_username: String,
    val password: String? = null
)


data class AddVaultRequest(
    val site_name: String,
    val site_url: String?,
    val site_username: String,
    val password: String
)


data class UpdateVaultRequest(
    val site_name: String,
    val site_url: String?,
    val site_username: String,
    val password: String
)


data class DecryptVaultResponse(
    val password: String
)
