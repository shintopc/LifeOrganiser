package com.shintopc.lifeorganiser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shintopc.lifeorganiser.ui.components.AppBottomBar
import com.shintopc.lifeorganiser.ui.components.AppTopBar
import com.shintopc.lifeorganiser.ui.components.Screen
import com.shintopc.lifeorganiser.ui.components.Sidebar
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import com.shintopc.lifeorganiser.ui.theme.LightBlueSidebar
import com.shintopc.lifeorganiser.ui.screens.HomeScreen
import com.shintopc.lifeorganiser.ui.screens.SettingsScreen
import com.shintopc.lifeorganiser.ui.screens.TasksScreen

// ... imports ...
import com.shintopc.lifeorganiser.ui.settings.SettingsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.MaterialTheme

@Composable
fun MainScreen(
    appLockViewModel: com.shintopc.lifeorganiser.ui.security.AppLockViewModel,
    settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val menuOrder by settingsViewModel.menuOrder.collectAsState()
    var showSystemBars by remember { mutableStateOf(true) }

    // Handle Back Press
    androidx.activity.compose.BackHandler(enabled = currentScreen != Screen.Home) {
        if (!showSystemBars) {
            showSystemBars = true
        } else {
            currentScreen = Screen.Home
        }
    }

    // Permission Logic for Notifications (Android 13+)
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { /* Handled */ }
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
             permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            if (showSystemBars) {
                AppTopBar(
                    title = when (currentScreen) {
                        Screen.Home -> "LIFE ORGANISER"
                        Screen.Hub -> "Explore"
                        Screen.Finance -> "Finance"
                        Screen.Tasks -> "Tasks"
                        Screen.ManageModules -> "Manage Modules"
                        else -> currentScreen.name // Fallback
                    },
                    currentScreen = currentScreen,
                    onToggleSidebar = { currentScreen = Screen.Hub }, // Open Hub 
                    onNavigateBack = { currentScreen = Screen.Home }
                )
            }
        },
        bottomBar = {
            if (showSystemBars) {
                AppBottomBar(
                    currentScreen = currentScreen,
                    menuOrder = menuOrder,
                    onNavigate = { currentScreen = it }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showSystemBars) paddingValues else PaddingValues(0.dp))
        ) {
            val onShowBars = { show: Boolean -> showSystemBars = show }
            when (currentScreen) {
                Screen.Home -> HomeScreen()
                Screen.Hub -> com.shintopc.lifeorganiser.ui.screens.HubScreen(
                    onNavigate = { currentScreen = it },
                    menuOrder = menuOrder
                )

                Screen.Settings -> com.shintopc.lifeorganiser.ui.screens.SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onNavigateToManageModules = { currentScreen = Screen.ManageModules }
                )
                Screen.ManageModules -> com.shintopc.lifeorganiser.ui.screens.ManageModulesScreen(
                    onNavigateBack = { currentScreen = Screen.Settings }
                )
                Screen.Documents -> com.shintopc.lifeorganiser.ui.documents.DocumentsFeature(onToggleBars = onShowBars)
                Screen.Passwords -> com.shintopc.lifeorganiser.ui.passwords.PasswordsFeature(onToggleBars = onShowBars)
                Screen.Reminders -> com.shintopc.lifeorganiser.ui.reminders.RemindersFeature(onToggleBars = onShowBars)
                Screen.Tasks -> TasksScreen(onToggleBars = onShowBars)
                Screen.Finance -> com.shintopc.lifeorganiser.ui.finance.FinanceFeature(onToggleBars = onShowBars)
                Screen.Notes -> com.shintopc.lifeorganiser.ui.notes.NotesFeature(onToggleBars = onShowBars)
                Screen.EmergencyInfo -> com.shintopc.lifeorganiser.ui.emergency.EmergencyInfoFeature(onToggleBars = onShowBars)
            }
        }
    }
}
