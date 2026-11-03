package com.shintopc.lifeorganiser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shintopc.lifeorganiser.data.local.FinanceDebt
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.homeState.collectAsState()
    val today = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
    
    var isClipboardOpen by remember { mutableStateOf(false) }
    var debtToClose by remember { mutableStateOf<FinanceDebt?>(null) }

    if (debtToClose != null) {
        AlertDialog(
            onDismissRequest = { debtToClose = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Close Debt?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Mark '${debtToClose?.personName}' as closed (paid)?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        debtToClose?.let { viewModel.closeDebt(it) }
                        debtToClose = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Debt", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToClose = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Welcome Header Card (Clean vertical stack - zero text overlap)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = today.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.greeting},",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (state.userName.isNotBlank()) state.userName else "User",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Quick Note Button
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        IconButton(
                            onClick = { isClipboardOpen = true },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val icon = if (state.clipboardContent.isNotBlank()) Icons.Default.Edit else Icons.Default.Add
                            Icon(
                                imageVector = icon,
                                contentDescription = "Quick Note",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Today's Timeline & Finance Summary
        item {
            CollapsibleSection(
                title = "Today's Schedule",
                itemCheck = state.timelineToday.size,
                initiallyExpanded = true
            ) {
                Column {
                    FinanceSummaryRow(state)
                    Spacer(modifier = Modifier.height(8.dp))
                    CategorizedTimelineView(items = state.timelineToday)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Upcoming Week
        item {
            if (state.timelineWeek.isNotEmpty()) {
                CollapsibleSection(
                    title = "Upcoming Week",
                    itemCheck = state.timelineWeek.size,
                    initiallyExpanded = false
                ) {
                    CategorizedTimelineView(
                        items = state.timelineWeek,
                        backgroundColor = Color.Transparent
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
        
        // Month
        item {
            if (state.timelineMonth.isNotEmpty()) {
                CollapsibleSection(
                    title = "Rest of Month",
                    itemCheck = state.timelineMonth.size,
                    initiallyExpanded = false
                ) {
                    TimelineList(items = state.timelineMonth)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }

    // --- Quick Note Dialog ---
    if (isClipboardOpen) {
        var text by remember { mutableStateOf(state.clipboardContent) }
        AlertDialog(
            onDismissRequest = { isClipboardOpen = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Quick Scratchpad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = { Text("Capture quick thoughts, links, or numbers...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveClipboard(text)
                        isClipboardOpen = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { 
                        viewModel.clearClipboard()
                        text = ""
                    }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { isClipboardOpen = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }
}
