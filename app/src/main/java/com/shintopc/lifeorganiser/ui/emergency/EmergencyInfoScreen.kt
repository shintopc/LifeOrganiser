package com.shintopc.lifeorganiser.ui.emergency

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shintopc.lifeorganiser.data.local.EmergencyContactEntity

@Composable
fun EmergencyInfoFeature(
    onToggleBars: (Boolean) -> Unit,
    viewModel: EmergencyViewModel = hiltViewModel()
) {
    EmergencyInfoScreen(viewModel, onToggleBars)
}

@Composable
fun EmergencyInfoScreen(
    viewModel: EmergencyViewModel,
    onToggleBars: (Boolean) -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContactEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<EmergencyContactEntity?>(null) }

    // Initial Population Logic (Run once)
    LaunchedEffect(Unit) {
        if (contacts.isEmpty()) { 
             // Ideally check if actually empty in DB to avoid flash, but simple check works for now
             // Or we rely on user manually adding? No, user expects some default categories.
             // We can check if list stays empty for a split second, or just let user add.
             // Let's provide a "Populate Default" button if empty, or just empty state.
             // Previous requirement had defaults. User asked to "add new fields".
             // Let's call populate if completely empty to maintain previous behavior feel.
             // Wait, contacts might take a moment to load from DB.
             // Safe to just show empty state with "Add" button for now.
             // But to restore the "Read-Only" values into DB, we should probably run it once.
             // Let's leave it to manual for now or provide a One-Time "Import Defaults" button in empty state.
        }
    }
    
    // Search
    var searchQuery by remember { mutableStateOf("") }
    
    // Grouping & Filtering
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts 
        else contacts.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.number.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }
    
    val groupedContacts = remember(filteredContacts) {
        filteredContacts.groupBy { it.category }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search emergency info...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No contacts found", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.populateDefaults() }) {
                            Text("Load Default Numbers")
                        }
                    }
                }
            } else if (filteredContacts.isEmpty()) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Matching Results", color = MaterialTheme.colorScheme.onSurfaceVariant)
                 }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    groupedContacts.forEach { (category, items) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                            )
                        }
                        items(items) { contact ->
                            EmergencyCard(
                                contact = contact,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(contact.number))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { editingContact = contact },
                                onDelete = { showDeleteConfirm = contact }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, number, category, note ->
                viewModel.addContact(name, number, category, note)
                showAddDialog = false
            }
        )
    }

    if (editingContact != null) {
        AddEditContactDialog(
            contact = editingContact,
            onDismiss = { editingContact = null },
            onSave = { name, number, category, note ->
                viewModel.updateContact(editingContact!!.copy(name = name, number = number, category = category, note = note))
                editingContact = null
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Contact?") },
            text = { Text("Are you sure you want to delete '${showDeleteConfirm?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteContact(it) }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun EmergencyCard(
    contact: EmergencyContactEntity,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                if (contact.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contact.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary
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
                            text = { Text("Edit") },
                            onClick = { 
                                showMenu = false
                                onEdit() 
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { 
                                showMenu = false
                                onDelete() 
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactDialog(
    contact: EmergencyContactEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var number by remember { mutableStateOf(contact?.number ?: "") }
    var category by remember { mutableStateOf(contact?.category ?: "Emergency Contacts") }
    var note by remember { mutableStateOf(contact?.note ?: "") }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Emergency Contacts", "Utility & Service Numbers", "Pension & Insurance", "Others")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact == null) "Add Contact" else "Edit Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name / Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Number / Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        readOnly = false, // Allow custom categories
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    isCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && number.isNotBlank() && category.isNotBlank()) {
                        onSave(name, number, category, note)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
