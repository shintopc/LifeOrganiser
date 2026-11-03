package com.shintopc.lifeorganiser.ui.passwords

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Lock 
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shintopc.lifeorganiser.data.local.PasswordEntity
import com.shintopc.lifeorganiser.ui.theme.BluePrimary

sealed interface PasswordsNav {
    object List : PasswordsNav
    object Add : PasswordsNav
    data class Edit(val password: PasswordEntity) : PasswordsNav
}

@Composable
fun PasswordsFeature(
    onToggleBars: (Boolean) -> Unit,
    passwordsViewModel: PasswordsViewModel = viewModel()
) {

    


    var navController by remember { mutableStateOf<PasswordsNav>(PasswordsNav.List) }
 
    val state = navController
    when (state) {
        PasswordsNav.Add, is PasswordsNav.Edit -> {
            LaunchedEffect(Unit) { onToggleBars(false) }
            AddPasswordScreen(
                passwordsViewModel = passwordsViewModel,
                passwordToEdit = (state as? PasswordsNav.Edit)?.password,
                onBack = { 
                    onToggleBars(true)
                    navController = PasswordsNav.List 
                },
                onSaveSuccess = { 
                    onToggleBars(true)
                    navController = PasswordsNav.List 
                }
            )
        }
        else -> {
            PasswordsScreen(
                passwordsViewModel = passwordsViewModel,
                onAddClick = { navController = PasswordsNav.Add },
                onEditClick = { navController = PasswordsNav.Edit(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordsScreen(
    passwordsViewModel: PasswordsViewModel,
    onAddClick: () -> Unit,
    onEditClick: (PasswordEntity) -> Unit
) {
    val passwords by passwordsViewModel.passwords.collectAsState()
    val searchQuery by passwordsViewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    
    // Delete Confirmation
    var passwordToDelete by remember { mutableStateOf<PasswordEntity?>(null) }

    if (passwordToDelete != null) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { passwordToDelete = null },
            title = { Text("Delete Password?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to delete '${passwordToDelete?.title}'?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    passwordToDelete?.let { passwordsViewModel.deletePassword(it) }
                    passwordToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { passwordToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Password")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search
            TextField(
                value = searchQuery,
                onValueChange = { passwordsViewModel.search(it) },
                placeholder = { Text("Search passwords...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(passwords) { password ->
                    PasswordItem(
                        password = password,
                        context = context,
                        onEditClick = { onEditClick(password) },
                        onDeleteClick = { passwordToDelete = password }
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordItem(
    password: PasswordEntity,
    context: Context,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        com.shintopc.lifeorganiser.util.ClipboardHelper.copyToClipboard(context, text)
    }

    val strength = calculatePasswordStrength(password.password)
    val strengthColor = getStrengthColor(strength)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Title + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WEBSITE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = password.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { showMenu = false; onEditClick() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDeleteClick() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Username Field
            Column {
                Text(
                    text = "USERNAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = password.username,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { copyToClipboard("Username", password.username) }
                    )
                    IconButton(
                        onClick = { copyToClipboard("Username", password.username) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copy Username",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            Column {
                Text(
                    text = "PASSWORD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isPasswordVisible) password.password else "•".repeat(password.password.length.coerceAtMost(10)),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Lock else Icons.Default.Face,
                            contentDescription = "Toggle Visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { copyToClipboard("Password", password.password) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copy Password",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                // Strength Bar
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(strengthColor.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (strength == 0) 0.33f else if (strength == 1) 0.66f else 1f)
                            .fillMaxHeight()
                            .background(strengthColor, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

fun calculatePasswordStrength(password: String): Int {
    if (password.length < 8) return 0 // Weak
    val hasDigits = password.any { it.isDigit() }
    val hasLetters = password.any { it.isLetter() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val hasMixedCase = password.any { it.isUpperCase() } && password.any { it.isLowerCase() }

    if (hasDigits && hasLetters && (hasSpecial || hasMixedCase)) return 2 // Strong
    if (hasDigits || (hasLetters && password.length >= 10)) return 1 // Medium
    return 0 // Weak
}

fun getStrengthColor(strength: Int): Color {
    return when (strength) {
        0 -> Color(0xFFE57373) // Red (Weak)
        1 -> Color(0xFFFFB74D) // Orange (Medium)
        2 -> Color(0xFF81C784) // Green (Strong)
        else -> Color.Gray
    }
}
