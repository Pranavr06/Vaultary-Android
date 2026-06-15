package com.vaultary.app.data.repository

import com.vaultary.app.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class DashboardRepository(private val api: VaultaryApi) {

    // --- Profile ---
    suspend fun getProfile(): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load profile: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProfile(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfile(): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProfile()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to delete profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Vault ---
    suspend fun getVaults(): Result<List<VaultItemResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getVaults()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch vaults"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addVault(request: AddVaultRequest): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.addVault(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add vault"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVaultItem(id: Int, request: UpdateVaultRequest): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateVaultItem(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update vault"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVaultItem(id: Int): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteVaultItem(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to delete vault"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decryptVaultItem(id: Int): Result<DecryptVaultResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.decryptVaultItem(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to decrypt password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportVault(): Result<ResponseBody> = withContext(Dispatchers.IO) {
        try {
            val response = api.exportVault()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to export vault"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2FA ---
    suspend fun setup2FA(): Result<TwoFactorSetupResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.setup2FA()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to setup 2FA"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enable2FA(request: TwoFactorEnableRequest): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.enable2FA(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to enable 2FA"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disable2FA(): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.disable2FA()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to disable 2FA"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Admin ---
    suspend fun getAllUsers(): Result<List<UserResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllUsers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminDeleteUser(userId: Int): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.adminDeleteUser(userId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to delete user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Tools ---
    suspend fun checkPassword(request: CheckPasswordRequest): Result<CheckPasswordResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkPassword(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to check password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(): Result<List<HistoryResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getHistory()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generatePassword(): Result<GeneratePasswordResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.generatePassword()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to generate password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Secure Notes ---
    suspend fun getNotes(): Result<List<NoteItemResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getNotes()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch notes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addNote(request: AddNoteRequest): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.addNote(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add note"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(id: Int, request: UpdateNoteRequest): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateNote(id, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update note"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(id: Int): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteNote(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to delete note"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decryptNote(id: Int): Result<DecryptNoteResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.decryptNote(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to decrypt note"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Contact ---
    suspend fun contactSupport(request: ContactRequest): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.contactSupport(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
