package com.shintopc.lifeorganiser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shintopc.lifeorganiser.ui.components.Screen
import com.shintopc.lifeorganiser.ui.settings.SettingsViewModel
import com.shintopc.lifeorganiser.ui.settings.ModuleSettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageModulesScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.moduleSettingsState.collectAsState()
    
    // Local state for drag and drop
    // We need a local list to mutate instantly while dragging
    var currentList by remember { mutableStateOf(state.enabledModules) }
    
    // Sync local list when upstream changes (unless we are dragging?)
    // Actually, we should sync only when not dragging or if the size changes drastically.
    // For simplicity, let's sync when state.enabledModules changes, IF we aren't dragging.
    val isDragging = remember { mutableStateOf(false) }
    
    LaunchedEffect(state.enabledModules) {
        if (!isDragging.value) {
            currentList = state.enabledModules
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Modules") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Section: Enabled Modules
                Text(
                    text = "Enabled Modules (Drag to reorder)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Drag Logic
                val dragOffset = remember { mutableStateOf(0f) }
                val draggedIndex = remember { mutableStateOf<Int?>(null) }
                val itemHeightPx = remember { mutableStateOf(0f) }

                // Determine item density for rough calculations (or usage of LayoutCoordinates)
                // For a simpler approach without complicated measuring:
                // Assume fixed height or use onGloballyPositioned on items.
                // Since items are identical Cards, we can assume similar height.
                
                Column {
                    currentList.forEachIndexed { index, screen ->
                        val isBeingDragged = draggedIndex.value == index
                        val offset = if (isBeingDragged) dragOffset.value else 0f
                        
                        // Z-Index: Dragged item on top
                        val zIndex = if (isBeingDragged) 1f else 0f
                        val scale = if (isBeingDragged) 1.05f else 1f
                        val shadow = if (isBeingDragged) 8.dp else 2.dp
                        
                        Box(
                            modifier = Modifier
                                .zIndex(zIndex)
                                .graphicsLayer {
                                    translationY = offset
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .fillMaxWidth()
                        ) {
                            EnabledModuleItem(
                                screen = screen,
                                isFirst = index == 0,
                                isLast = index == currentList.size - 1,
                                onToggle = { viewModel.toggleModule(screen, false) },
                                onMoveUp = { 
                                    // Fallback Arrow Support
                                    if (index > 0) {
                                       val newList = currentList.toMutableList()
                                       java.util.Collections.swap(newList, index, index - 1)
                                       currentList = newList
                                       draggedIndex.value = index - 1 // Fix: Update dragged index to match move
                                       viewModel.updateOrder(newList)
                                    }
                                },
                                onMoveDown = {
                                    // Fallback Arrow Support
                                    if (index < currentList.size - 1) {
                                       val newList = currentList.toMutableList()
                                       java.util.Collections.swap(newList, index, index + 1)
                                       currentList = newList
                                       draggedIndex.value = index + 1 // Fix: Update dragged index to match move
                                       viewModel.updateOrder(newList)
                                    }
                                },
                                // Drag Handle Modifier
                                dragModifier = Modifier.pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { 
                                            isDragging.value = true
                                            draggedIndex.value = index
                                            dragOffset.value = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset.value += dragAmount.y
                                            
                                            // Swap Logic
                                            // If we dragged more than itemHeight/2 up or down
                                            // We need to know itemHeight. Let's estimate or capture it.
                                            // A card is approx 70-80dp?
                                            // improved: use layouts. But for now, let's assume ~72dp + 8dp spacer = 80dp * density.
                                            // Better: onGloballyPositioned
                                            val currentItemHeight = itemHeightPx.value
                                            if (currentItemHeight > 0) {
                                                val threshold = currentItemHeight * 0.7f // slightly more than half to be stable
                                                
                                                if (dragOffset.value > threshold) {
                                                    // Moved Down
                                                    if (index < currentList.size - 1) {
                                                        val newList = currentList.toMutableList()
                                                        java.util.Collections.swap(newList, index, index + 1)
                                                        currentList = newList
                                                        // Update state to track the NEW index of the dragged item
                                                        draggedIndex.value = index + 1
                                                        dragOffset.value -= currentItemHeight
                                                    }
                                                } else if (dragOffset.value < -threshold) {
                                                    // Moved Up
                                                    if (index > 0) {
                                                        val newList = currentList.toMutableList()
                                                        java.util.Collections.swap(newList, index, index - 1)
                                                        currentList = newList
                                                        draggedIndex.value = index - 1
                                                        dragOffset.value += currentItemHeight
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            isDragging.value = false
                                            draggedIndex.value = null
                                            dragOffset.value = 0f
                                            // Commit changes
                                            viewModel.updateOrder(currentList)
                                        },
                                        onDragCancel = {
                                            isDragging.value = false
                                            draggedIndex.value = null
                                            dragOffset.value = 0f
                                            // Revert? Or just commit what we have? Commit is safer UX usually.
                                            currentList = state.enabledModules // Revert
                                        }
                                    )
                                },
                                onHeightMeasured = { height ->
                                    // Capture height of items (assuming uniform)
                                    if (height > 0) itemHeightPx.value = height.toFloat() + 20f // Add margin/spacer buffer approx (8dp -> ~20px)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))

                // Section: Disabled Modules
                Text(
                    text = "Disabled Modules",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (state.disabledModules.isEmpty()) {
                    Text(
                        text = "No disabled modules",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    state.disabledModules.forEach { screen ->
                        DisabledModuleItem(
                            screen = screen,
                            onToggle = { viewModel.toggleModule(screen, true) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EnabledModuleItem(
    screen: Screen,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    dragModifier: Modifier = Modifier,
    onHeightMeasured: (Int) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                onHeightMeasured(coordinates.size.height)
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reorder Controls (Up/Down Arrows)
                Column {
                   if (!isFirst) {
                       Icon(
                           imageVector = Icons.Default.KeyboardArrowUp,
                           contentDescription = "Move Up",
                           modifier = Modifier
                               .size(24.dp)
                               .clickable { onMoveUp() }
                               .padding(2.dp),
                           tint = MaterialTheme.colorScheme.onSurfaceVariant
                       )
                   } else {
                       Spacer(modifier = Modifier.size(24.dp))
                   }
                   
                   if (!isLast) {
                       Icon(
                           imageVector = Icons.Default.KeyboardArrowDown,
                           contentDescription = "Move Down",
                           modifier = Modifier
                               .size(24.dp)
                               .clickable { onMoveDown() }
                               .padding(2.dp),
                           tint = MaterialTheme.colorScheme.onSurfaceVariant
                       )
                   } else {
                        Spacer(modifier = Modifier.size(24.dp))
                   }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Drag Handle Visual
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = dragModifier
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = getScreenName(screen),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Switch(
                checked = true,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun DisabledModuleItem(
    screen: Screen,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(48.dp)) // Indent to match text of enabled items
                
                Text(
                    text = getScreenName(screen),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
                )
            }

            Switch(
                checked = false,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

fun getScreenName(screen: Screen): String {
    return when(screen) {
        Screen.Home -> "Home" // Should not happen in list? Requirement said "Main modules". Home is likely fixed.
        Screen.Settings -> "Settings"
        Screen.Hub -> "Hub"
        Screen.EmergencyInfo -> "Emergency Info"
        else -> screen.name
    }
}

