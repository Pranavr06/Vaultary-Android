package com.vaultary.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VaultaryApi {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("login/verify_2fa")
    suspend fun verify2fa(@Body request: Verify2faRequest): Response<Verify2faResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("forgot_password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse>

    @POST("reset_password_confirm")
    suspend fun resetPasswordConfirm(@Body request: ResetPasswordRequest): Response<ApiResponse>

    // --- Profile ---
    @retrofit2.http.GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @retrofit2.http.PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse>

    @retrofit2.http.DELETE("profile")
    suspend fun deleteProfile(): Response<ApiResponse>

    // --- Vault ---
    @retrofit2.http.GET("vault")
    suspend fun getVaults(): Response<List<VaultItemResponse>>

    @POST("vault")
    suspend fun addVault(@Body request: AddVaultRequest): Response<ApiResponse>

    @retrofit2.http.PUT("vault/item/{item_id}")
    suspend fun updateVaultItem(@retrofit2.http.Path("item_id") itemId: Int, @Body request: UpdateVaultRequest): Response<ApiResponse>

    @retrofit2.http.DELETE("vault/delete/{item_id}")
    suspend fun deleteVaultItem(@retrofit2.http.Path("item_id") itemId: Int): Response<ApiResponse>

    @POST("vault/decrypt/{item_id}")
    suspend fun decryptVaultItem(@retrofit2.http.Path("item_id") itemId: Int): Response<DecryptVaultResponse>

    @retrofit2.http.Streaming
    @retrofit2.http.GET("vault/export")
    suspend fun exportVault(): Response<okhttp3.ResponseBody>

    // --- 2FA ---
    @POST("2fa/setup")
    suspend fun setup2FA(): Response<TwoFactorSetupResponse>

    @POST("2fa/enable")
    suspend fun enable2FA(@Body request: TwoFactorEnableRequest): Response<ApiResponse>

    @POST("2fa/disable")
    suspend fun disable2FA(): Response<ApiResponse>

    // --- Admin ---
    @retrofit2.http.GET("admin/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @retrofit2.http.DELETE("admin/delete/{user_id}")
    suspend fun adminDeleteUser(@retrofit2.http.Path("user_id") userId: Int): Response<ApiResponse>

    // --- Tools ---
    @POST("check_password")
    suspend fun checkPassword(@Body request: CheckPasswordRequest): Response<CheckPasswordResponse>

    @retrofit2.http.GET("history")
    suspend fun getHistory(): Response<List<HistoryResponse>>

    @retrofit2.http.GET("generate_password")
    suspend fun generatePassword(): Response<GeneratePasswordResponse>

    // --- Secure Notes ---
    @retrofit2.http.GET("notes")
    suspend fun getNotes(): Response<List<NoteItemResponse>>

    @POST("notes")
    suspend fun addNote(@Body request: AddNoteRequest): Response<ApiResponse>

    @retrofit2.http.PUT("notes/{id}")
    suspend fun updateNote(@retrofit2.http.Path("id") id: Int, @Body request: UpdateNoteRequest): Response<ApiResponse>

    @retrofit2.http.DELETE("notes/{id}")
    suspend fun deleteNote(@retrofit2.http.Path("id") id: Int): Response<ApiResponse>

    @POST("notes/decrypt/{id}")
    suspend fun decryptNote(@retrofit2.http.Path("id") id: Int): Response<DecryptNoteResponse>

    // --- Contact ---
    @POST("contact")
    suspend fun contactSupport(@Body request: ContactRequest): Response<ApiResponse>
}
