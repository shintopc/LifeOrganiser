package com.shintopc.lifeorganiser.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.ui.profile.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

import com.shintopc.lifeorganiser.ui.settings.SettingsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.shintopc.lifeorganiser.ui.components.Screen
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToManageModules: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadSecuritySettings()
    }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth() // Changed from fillMaxSize to avoid scroll issues
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 80.dp), // Space for FAB/Button
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // --- Profile Image Removed as per User Request ---

                Spacer(modifier = Modifier.height(20.dp))

                ProfileTextField(
                    value = profileViewModel.name,
                    onValueChange = { profileViewModel.name = it },
                    label = "Full Name",
                    icon = Icons.Default.Person
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        profileViewModel.saveProfile()
                        scope.launch {
                            snackbarHostState.showSnackbar("Settings saved successfully")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                Spacer(modifier = Modifier.height(20.dp))

                // --- Appearance ---
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                val currentTheme by settingsViewModel.themeMode.collectAsState()
                val themeOptions = listOf("SYSTEM", "LIGHT", "DARK")
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    themeOptions.forEach { theme ->
                        val isSelected = currentTheme == theme
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { settingsViewModel.setTheme(theme) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when(theme) {
                                    "SYSTEM" -> "System"
                                    "LIGHT" -> "Light"
                                    "DARK" -> "Dark"
                                    else -> theme
                                },
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                Spacer(modifier = Modifier.height(20.dp))

                // --- Security ---
                Text(
                    text = "App Security",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                         Text(text = "Biometric Lock", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                         Text(text = "Require fingerprint/face to open app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = profileViewModel.isBiometricEnabled,
                        onCheckedChange = { profileViewModel.setBiometricLock(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))
                
                // --- Appearance section moved up ---

                Spacer(modifier = Modifier.height(20.dp))

                // --- Layout Settings ---
                Text(
                    text = "Layout",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedButton(
                    onClick = onNavigateToManageModules,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manage Modules")
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))

                // --- Data Management ---
                Text(
                    text = "Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))

                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri: Uri? ->
                    if (uri != null) {
                        scope.launch {
                            val success = profileViewModel.exportData(uri)
                            if (success) {
                                snackbarHostState.showSnackbar("Data exported successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to export data")
                            }
                        }
                    }
                }

                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    if (uri != null) {
                        scope.launch {
                            val success = profileViewModel.importData(uri)
                            if (success) {
                                snackbarHostState.showSnackbar("Data imported successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to import data")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch("LifeOrganiser_Backup_${System.currentTimeMillis()}.json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) // Using ArrowDownward icon for Export (Save)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) // Using ArrowUpward icon for Import
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // --- Device Migration ---
                val exportPortableLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri: Uri? ->
                    if (uri != null) {
                        scope.launch {
                            val success = profileViewModel.exportDataPortable(uri)
                            if (success) {
                                snackbarHostState.showSnackbar("Migration file created! (Has readable passwords)")
                            } else {
                                snackbarHostState.showSnackbar("Failed to create migration file")
                            }
                        }
                    }
                }

                val importPortableLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    if (uri != null) {
                        scope.launch {
                            val success = profileViewModel.importDataPortable(uri)
                            if (success) {
                                snackbarHostState.showSnackbar("Data migrated and secured!")
                            } else {
                                snackbarHostState.showSnackbar("Failed to migrate data")
                            }
                        }
                    }
                }

                Text(
                     text = "Device Migration (New Phone)",
                     style = MaterialTheme.typography.titleMedium,
                     color = MaterialTheme.colorScheme.secondary,
                     modifier = Modifier.align(Alignment.Start)
                )
                 Text(
                     text = "Use this to move data to a completely new device.",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { exportPortableLauncher.launch("LifeOrganiser_Migration_${System.currentTimeMillis()}.json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                         colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export for New Phone", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { importPortableLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                         colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)

                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import from Old Phone", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var showResetDialog by remember { mutableStateOf(false) }

                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset All Data") },
                        text = { Text("Are you sure you want to delete all app data? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        val success = profileViewModel.clearData()
                                        if (success) {
                                            snackbarHostState.showSnackbar("All data cleared")
                                        } else {
                                            snackbarHostState.showSnackbar("Failed to clear data")
                                        }
                                    }
                                    showResetDialog = false
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete All")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }

                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Data")
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                // Footer
                val appVersion = remember {
                    try {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        pInfo.versionName
                    } catch (e: Exception) {
                        "Unknown"
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                     Text(
                        text = "Version $appVersion",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                     Text(
                        text = "Developed by Shinto PC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }


        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = maxLines == 1,
        maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun MenuReorderDialog(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val menuOrder by viewModel.menuOrder.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rearrange Menu") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Use arrows to reorder items.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                menuOrder.forEach { screen ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(screen.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        
                        Row {
                            IconButton(
                                onClick = { viewModel.moveUp(screen) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(
                                onClick = { viewModel.moveDown(screen) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface // Dialog background
    )
}
