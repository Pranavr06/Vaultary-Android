package com.vaultary.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VaultaryApi {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("login/verify_2fa")
    suspend fun verify2fa(@Body request: Verify2faRequest): Response<Verify2faResponse>

}
