package com.vaultary.app.data.remote

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class Verify2faRequest(
    @SerializedName("temp_token") val tempToken: String,
    @SerializedName("code") val code: String
)
