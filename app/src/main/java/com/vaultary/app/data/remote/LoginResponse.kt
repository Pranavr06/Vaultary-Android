package com.vaultary.app.data.remote

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("temp_token") val tempToken: String?,
    @SerializedName("is_admin") val isAdmin: Boolean?
)

data class Verify2faResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("is_admin") val isAdmin: Boolean?
)
