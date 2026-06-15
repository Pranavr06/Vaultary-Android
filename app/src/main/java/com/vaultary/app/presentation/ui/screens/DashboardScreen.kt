package com.vaultary.app.presentation.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import com.vaultary.app.data.repository.DashboardRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaultary.app.presentation.dashboard.ProfileViewModel

enum class DashboardTab(val title: String, val icon: ImageVector) {
    PROFILE("Profile", Icons.Filled.Person),
    TOOLS("Tools", Icons.Filled.Build),
    VAULT("Vault", Icons.Filled.Lock),
    NOTES("Notes", Icons.Filled.Notes),
    ADMIN("Admin", Icons.Filled.Security)
}

@Composable
fun DashboardScreen(
    isAdmin: Boolean = false,
    dashboardRepository: DashboardRepository,
    onLogout: () -> Unit,
    onNavigateTo2FASetup: () -> Unit,
    onNavigateToAddEditVault: (Int?) -> Unit,
    onNavigateToAddEditNote: (Int?) -> Unit,
    isDarkTheme: Boolean = true,
    onThemeToggle: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(DashboardTab.TOOLS) }

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(dashboardRepository)
    )
    val profileState by profileViewModel.uiState.collectAsState()
    val username = (profileState as? com.vaultary.app.presentation.dashboard.ProfileUiState.Success)?.profile?.username ?: "Vaultary"

    val tabs = if (isAdmin) {
        DashboardTab.values().toList()
    } else {
        DashboardTab.values().filter { it != DashboardTab.ADMIN }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(username, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                DashboardTab.PROFILE -> {
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onLogout = onLogout,
                        onNavigateTo2FASetup = onNavigateTo2FASetup
                    )
                }
                DashboardTab.VAULT -> {
                    val vaultViewModel: com.vaultary.app.presentation.dashboard.VaultViewModel = viewModel(
                        factory = com.vaultary.app.presentation.dashboard.VaultViewModel.Factory(dashboardRepository)
                    )
                    VaultScreen(
                        viewModel = vaultViewModel,
                        onNavigateToAddEdit = onNavigateToAddEditVault
                    )
                }
                DashboardTab.TOOLS -> {
                    val toolsViewModel: com.vaultary.app.presentation.dashboard.ToolsViewModel = viewModel(
                        factory = com.vaultary.app.presentation.dashboard.ToolsViewModel.Factory(dashboardRepository)
                    )
                    ToolsScreen(viewModel = toolsViewModel)
                }
                DashboardTab.NOTES -> {
                    val notesViewModel: com.vaultary.app.presentation.ui.screens.NotesViewModel = viewModel(
                        factory = com.vaultary.app.presentation.ui.screens.NotesViewModel.Factory(dashboardRepository)
                    )
                    NotesScreen(
                        viewModel = notesViewModel,
                        onNavigateToAddEdit = onNavigateToAddEditNote
                    )
                }
                DashboardTab.ADMIN -> {
                    val adminViewModel: com.vaultary.app.presentation.dashboard.AdminViewModel = viewModel(
                        factory = com.vaultary.app.presentation.dashboard.AdminViewModel.Factory(dashboardRepository)
                    )
                    AdminScreen(viewModel = adminViewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileScreenPlaceholder() {
    Text("Profile Screen", color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun VaultScreenPlaceholder() {
    Text("Vault Screen", color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun ToolsScreenPlaceholder() {
    Text("Tools Screen", color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun AdminScreenPlaceholder() {
    Text("Admin Screen", color = MaterialTheme.colorScheme.onBackground)
}
