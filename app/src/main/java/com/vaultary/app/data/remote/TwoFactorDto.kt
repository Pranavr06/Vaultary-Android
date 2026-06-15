package com.vaultary.app.data.remote




data class TwoFactorSetupResponse(
    val secret: String,
    val qr_image: String
)


data class TwoFactorEnableRequest(
    val code: String
)
