package com.shintopc.lifeorganiser.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shintopc.lifeorganiser.data.local.ReminderEntity
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

sealed interface RemindersNav {
    object Main : RemindersNav
    object Add : RemindersNav
    data class Edit(val reminder: ReminderEntity) : RemindersNav
}

@Composable
fun RemindersFeature(
    onToggleBars: (Boolean) -> Unit,
    remindersViewModel: RemindersViewModel = viewModel()
) {
    var navState by remember { mutableStateOf<RemindersNav>(RemindersNav.Main) }
    
    val selectedDate by remindersViewModel.selectedDate.collectAsState()

    val state = navState
    when (state) {
        RemindersNav.Add -> {
            LaunchedEffect(Unit) { onToggleBars(false) }
            AddReminderScreen(
                remindersViewModel = remindersViewModel,
                initialDate = selectedDate,
                onBack = { 
                    onToggleBars(true)
                    navState = RemindersNav.Main 
                },
                onSaveSuccess = { 
                    onToggleBars(true)
                    navState = RemindersNav.Main 
                }
            )
        }
        is RemindersNav.Edit -> {
             LaunchedEffect(Unit) { onToggleBars(false) }
             AddReminderScreen(
                remindersViewModel = remindersViewModel,
                initialDate = selectedDate,
                reminderToEdit = state.reminder,
                onBack = { 
                    onToggleBars(true)
                    navState = RemindersNav.Main 
                },
                onSaveSuccess = { 
                    onToggleBars(true)
                    navState = RemindersNav.Main 
                }
            )
        }
        else -> {
            RemindersScreen(
                remindersViewModel = remindersViewModel,
                onAddClick = { navState = RemindersNav.Add },
                onReminderClick = { navState = RemindersNav.Edit(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    remindersViewModel: RemindersViewModel,
    onAddClick: () -> Unit,
    onReminderClick: (ReminderEntity) -> Unit
) {
    val currentMonth by remindersViewModel.currentMonth.collectAsState()
    val selectedDate by remindersViewModel.selectedDate.collectAsState()
    val eventsMap by remindersViewModel.calendarEvents.collectAsState()
    val reminders by remindersViewModel.remindersForSelectedDate.collectAsState()
    val upcomingReminders by remindersViewModel.upcomingReminders.collectAsState()
    var reminderToDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Delete Reminder?") },
            text = { Text("Are you sure you want to delete '${reminderToDelete?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderToDelete?.let { remindersViewModel.deleteReminder(context, it) }
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) { Text("Cancel") }
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
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Calendar Component
            CalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                eventsMap = eventsMap,
                onDateSelected = { remindersViewModel.setSelectedDate(it) },
                onMonthChanged = { remindersViewModel.setCurrentMonth(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Events on ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            if (reminders.isEmpty() && upcomingReminders.isEmpty()) {
                com.shintopc.lifeorganiser.ui.components.EmptyState(
                    message = "No events today or upcoming. Add one!",
                    icon = androidx.compose.material.icons.Icons.Default.Add,
                    showArrow = true
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (reminders.isEmpty()) {
                        item {
                           Box(
                               modifier = Modifier
                                   .fillMaxWidth()
                                   .padding(vertical = 32.dp),
                               contentAlignment = Alignment.Center
                           ) {
                               Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                      imageVector = Icons.Filled.Info,
                                      contentDescription = null,
                                      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                      modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No events for today",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                               }
                           }
                        }
                    } else {
                        items(reminders) { reminder ->
                            ReminderItem(
                                reminder = reminder,
                                onClick = { onReminderClick(reminder) },
                                onDeleteClick = { reminderToDelete = reminder }
                            )
                        }
                    }
                    
                    if (upcomingReminders.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item {
                             Text(
                                text = "Upcoming (Next 7 Days)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(upcomingReminders) { reminder ->
                             ReminderItem(
                                reminder = reminder,
                                onClick = { onReminderClick(reminder) },
                                onDeleteClick = { reminderToDelete = reminder }
                            )
                        }
                    }
    
                    item { Spacer(modifier = Modifier.height(80.dp)) } // FAB spacing
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: ReminderEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(reminder.color))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Name -> Bold
                Text(
                    text = reminder.title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Relative Date -> Medium
                val relativeTime = com.shintopc.lifeorganiser.util.DateUtils.getRelativeTimeString(reminder.dateTime)
                Text(
                    text = relativeTime,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Absolute Date -> Small / Secondary
                val absoluteDate = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
                    .withZone(java.time.ZoneId.systemDefault())
                    .format(java.time.Instant.ofEpochMilli(reminder.dateTime))

                Text(
                    text = absoluteDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (reminder.repeatMode != "NONE") {
                    Spacer(modifier = Modifier.height(4.dp))
                     Text(
                        text = "Repeats: ${reminder.repeatMode}", 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            

            
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
            }
        }
    }
}


