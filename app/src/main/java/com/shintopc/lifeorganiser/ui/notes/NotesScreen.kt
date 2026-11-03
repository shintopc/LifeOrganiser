package com.shintopc.lifeorganiser.ui.notes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically


import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.shintopc.lifeorganiser.data.local.NoteEntity
import com.shintopc.lifeorganiser.ui.theme.BluePrimary

// Pastel Colors
val NoteColors = listOf(
    Color(0xFFFFFFFF), // White
    Color(0xFFF28B82), // Red
    Color(0xFFFBBC04), // Orange
    Color(0xFFFFF475), // Yellow
    Color(0xFFCCFF90), // Green
    Color(0xFFA7FFEB), // Teal
    Color(0xFFCBF0F8), // Blue
    Color(0xFFAECBFA), // Dark Blue
    Color(0xFFD7AEFB), // Purple
    Color(0xFFFDCFE8), // Pink
    Color(0xFFE6C9A8), // Brown
    Color(0xFFE0E0E0)  // Gray
)

@Composable
fun NotesFeature(
    onToggleBars: (Boolean) -> Unit,
    notesViewModel: NotesViewModel = hiltViewModel()
) {
    NotesScreen(notesViewModel, onToggleBars)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onToggleBars: (Boolean) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var isCreatingNote by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note?") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        noteToDelete = null
                        onToggleBars(true)
                        isCreatingNote = false
                        editingNote = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (isCreatingNote || editingNote != null) {
        LaunchedEffect(Unit) { onToggleBars(false) }
        NoteEditorScreen(
            note = editingNote,
            onDismiss = { 
                onToggleBars(true)
                isCreatingNote = false 
                editingNote = null
            },
            onSave = { title, content, color, isPinned ->
                if (editingNote != null) {
                    viewModel.updateNote(editingNote!!.copy(title = title, content = content, color = color, isPinned = isPinned))
                } else {
                    viewModel.addNote(title, content, color, isPinned)
                }
                onToggleBars(true)
                isCreatingNote = false
                editingNote = null
            },
            onDelete = {
                if (editingNote != null) {
                    noteToDelete = editingNote
                }
            }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                FloatingActionButton(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        isCreatingNote = true 
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.search(it) },
                    placeholder = { Text("Search your notes", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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

                if (notes.isEmpty()) {
                    com.shintopc.lifeorganiser.ui.components.EmptyState(
                        message = "No notes yet. Tap + to write something!",
                        icon = androidx.compose.material.icons.Icons.Default.Favorite
                    )
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notes) { note ->
                            NoteCard(
                                note = note,
                                onClick = { editingNote = note }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit
) {
    val isDefaultColor = note.color == 0 || note.color == 1 || note.color == Color(0xFFFFFFFF).toArgb()
    val cardColor = if (isDefaultColor) MaterialTheme.colorScheme.surface else Color(note.color)
    val isWhiteish = isDefaultColor || (cardColor.red > 0.85f && cardColor.green > 0.85f && cardColor.blue > 0.85f)
    
    val contentColor = if (isDefaultColor) {
        MaterialTheme.colorScheme.onSurface
    } else {
        val brightness = (cardColor.red * 0.299f + cardColor.green * 0.587f + cardColor.blue * 0.114f)
        if (brightness < 0.5f) Color.White else Color(0xFF0F172A)
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick()
            }
            .border(
                width = 1.dp,
                color = if (isWhiteish) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (note.title.isNotEmpty()) {
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = contentColor,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp)
                    )
                }
                
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Pinned",
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(start = 4.dp) // padding from title
                    )
                }
            }
            
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 10,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: NoteEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var color by remember { mutableIntStateOf(note?.color ?: Color(0xFFFFFFFF).toArgb()) }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }
    var showColorPicker by remember { mutableStateOf(false) }

    // Auto-Save Function
    val saveNote = {
        if (title.isNotBlank() || content.isNotBlank()) {
            onSave(title, content, color, isPinned)
        }
    }

    // Handle Back Press
    androidx.activity.compose.BackHandler {
        saveNote()
        onDismiss()
    }

    val isDefaultColor = color == 0 || color == 1 || color == Color(0xFFFFFFFF).toArgb()
    val screenBg = if (isDefaultColor) MaterialTheme.colorScheme.background else Color(color)
    val contentColor = if (isDefaultColor) {
        MaterialTheme.colorScheme.onBackground
    } else {
        val c = Color(color)
        val brightness = (c.red * 0.299f + c.green * 0.587f + c.blue * 0.114f)
        if (brightness < 0.5f) Color.White else Color(0xFF0F172A)
    }

    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { 
                        saveNote()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = contentColor)
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            if (isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Pin",
                            tint = contentColor
                        )
                    }
                    if (note != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = contentColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(screenBg)
            ) {
               // Inline Color Picker (Animated)
                androidx.compose.animation.AnimatedVisibility(
                    visible = showColorPicker,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha=0.05f)) // Slight contrast for picker area
                    ) {
                        items(NoteColors) { c ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(2.dp, if (color == c.toArgb()) Color.Black.copy(alpha=0.5f) else Color.LightGray, CircleShape)
                                    .clickable { color = c.toArgb() }
                            )
                        }
                    }
                }

                // Bottom Bar Controls
                BottomAppBar(
                    containerColor = Color.Transparent, 
                    actions = {
                        IconButton(onClick = { showColorPicker = !showColorPicker }) {
                            Icon(Icons.Default.Edit, contentDescription = "Color", tint = contentColor)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Edited " + java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.5f),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    // No FAB needed for Auto-Save
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha=0.6f)) },
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = contentColor),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = contentColor,
                    focusedTextColor = contentColor,
                    unfocusedTextColor = contentColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Note", color = contentColor.copy(alpha=0.6f)) },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, lineHeight = 28.sp, color = contentColor),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = contentColor,
                    focusedTextColor = contentColor,
                    unfocusedTextColor = contentColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
