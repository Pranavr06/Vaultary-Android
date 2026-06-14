package com.vaultary.app.data.repository

import com.vaultary.app.data.local.TokenManager
import com.vaultary.app.data.remote.LoginRequest
import com.vaultary.app.data.remote.LoginResponse
import com.vaultary.app.data.remote.VaultaryApi
import com.vaultary.app.data.remote.Verify2faRequest
import com.vaultary.app.data.remote.Verify2faResponse

class AuthRepository(
    private val api: VaultaryApi,
    private val tokenManager: TokenManager
) {

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.status == "success" && body.token != null) {
                        tokenManager.saveToken(body.token)
                    }
                    // For 2fa_required, we do NOT save the temp_token here, we return it to the ViewModel
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verify2fa(request: Verify2faRequest): Result<Verify2faResponse> {
        return try {
            val response = api.verify2fa(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.status == "success" && body.token != null) {
                        tokenManager.saveToken(body.token)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("2FA verification failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        tokenManager.deleteToken()
    }
    
    fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }
}
