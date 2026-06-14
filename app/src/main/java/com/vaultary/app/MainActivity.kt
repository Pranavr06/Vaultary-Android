package com.vaultary.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.vaultary.app.data.local.TokenManager
import com.vaultary.app.data.remote.RetrofitInstance
import com.vaultary.app.data.repository.AuthRepository
import com.vaultary.app.presentation.auth.AuthViewModel
import com.vaultary.app.presentation.ui.navigation.VaultaryNavGraph
import com.vaultary.app.ui.theme.VaultaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual Dependency Injection
        val tokenManager = TokenManager(applicationContext)
        val api = RetrofitInstance.getApi(tokenManager)
        val repository = AuthRepository(api, tokenManager)
        val factory = AuthViewModel.Factory(repository)
        val viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setContent {
            VaultaryTheme {
                VaultaryNavGraph(viewModel = viewModel)
            }
        }
    }
}