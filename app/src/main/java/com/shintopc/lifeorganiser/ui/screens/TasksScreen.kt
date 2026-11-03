package com.shintopc.lifeorganiser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material.icons.filled.Info


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.data.model.Task
import com.shintopc.lifeorganiser.data.model.TaskPriority
import com.shintopc.lifeorganiser.ui.theme.TaskAccent
import com.shintopc.lifeorganiser.ui.theme.EmergencyAccent
import com.shintopc.lifeorganiser.ui.theme.ReminderAccent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.horizontalScroll
import java.time.format.DateTimeFormatter
import com.shintopc.lifeorganiser.data.local.SubTask
import com.shintopc.lifeorganiser.data.local.TaskCategory
import kotlinx.coroutines.launch


import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onToggleBars: (Boolean) -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    var showEditor by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    
    // UI State for Tip Card
    var showTip by remember { mutableStateOf(true) } // Could be persisted in DataStore
    
    // UI State for Confirmation Dialog
    var pendingAction by remember { mutableStateOf<TaskAction?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val categories by viewModel.categories.collectAsState()
    
    // Subtasks for editing
    val subtasksState = remember(taskToEdit) {
         if(taskToEdit != null) viewModel.getSubTasks(taskToEdit!!.id) else kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val currentSubtasks by subtasksState.collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Confirmation Dialog
    if (pendingAction != null) {
        val action = pendingAction!!
        val title = when(action) {
            is TaskAction.Complete -> "Mark as Completed?"
            is TaskAction.UnComplete -> "Mark as Pending?"
            is TaskAction.Delete -> "Move to Trash?"
            is TaskAction.Restore -> "Restore Task?"
            is TaskAction.PermanentDelete -> "Delete Permanently?"
        }
        val text = when(action) {
            is TaskAction.PermanentDelete -> "This action cannot be undone."
            else -> "Are you sure you want to proceed?"
        }
        
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = {
                    when(action) {
                        is TaskAction.Complete -> viewModel.toggleCompletion(context, action.task)
                        is TaskAction.UnComplete -> viewModel.toggleCompletion(context, action.task)
                        is TaskAction.Delete -> viewModel.removeTask(action.task)
                        is TaskAction.Restore -> viewModel.restoreTask(action.task)
                        is TaskAction.PermanentDelete -> viewModel.deleteTaskPermanently(action.task)
                    }
                    pendingAction = null
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) { Text("Cancel") }
            }
        )
    }

    if (showEditor) {
        LaunchedEffect(Unit) { onToggleBars(false) }
        TaskEditorScreen(
            task = taskToEdit,
            categories = categories,
            initialSubtasks = if(taskToEdit!=null) currentSubtasks else emptyList(),
            onDismiss = {
                onToggleBars(true)
                showEditor = false
                taskToEdit = null
            },
            onAddCategory = { name, color -> viewModel.addCategory(name, color) },
            onSave = { title, desc, priority, dueDate, dueTime, isReminderOn, isAlarm, subs ->
                // Ensure date is set if reminder is on
                val finalDueDate = if (isReminderOn && dueDate == null) System.currentTimeMillis() else dueDate
                
                if (taskToEdit != null) {
                    viewModel.updateTask(context, taskToEdit!!.copy(
                        title = title, 
                        description = desc, 
                        priority = priority,
                        dueDate = finalDueDate,
                        dueTime = dueTime,
                        isReminderOn = isReminderOn,
                        isAlarm = isAlarm
                    ), subs)
                } else {
                    viewModel.addTask(context, Task(
                        title = title, 
                        description = desc, 
                        priority = priority,
                        dueDate = finalDueDate,
                        dueTime = dueTime,
                        isReminderOn = isReminderOn,
                        isAlarm = isAlarm
                    ), subs)
                }
                onToggleBars(true)
                showEditor = false
                taskToEdit = null
            }
        )
    } else {

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    taskToEdit = null
                    showEditor = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- Sort & Filter Controls (Simplified) ---
                val sortOption by viewModel.sortOption.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                var showSortMenu by remember { mutableStateOf(false) }
                // Calendar view toggle - kept simple
                var showCalendar by remember { mutableStateOf(false) }

                // Header
                Row(
                   modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   Text("My Tasks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                   IconButton(onClick = { showCalendar = !showCalendar }) {
                       Icon(
                           imageVector = if (showCalendar) Icons.Default.Menu else Icons.Filled.Info,
                           contentDescription = "Toggle View"
                       )
                   }
               }
                
               if (!showCalendar) {
                   if (showTip) {
                       TipCard(onDismiss = { showTip = false })
                   }
                   
                   // Sections
                   val activeTasks = tasks.filter { !it.first.isCompleted && !it.first.isDeleted }
                   val completedTasks = tasks.filter { it.first.isCompleted && !it.first.isDeleted }
                   val deletedTasks = tasks.filter { it.first.isDeleted }

                   LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                   ) {
                       // PENDING
                       if (activeTasks.isNotEmpty()) {
                           item { Text("Pending", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                           items(items = activeTasks, key = { it.first.id }) { (task, subtasks) ->
                               SwipeableTaskItem(
                                   task = task,
                                   subtasks = subtasks,
                                   onRightSwipe = { pendingAction = TaskAction.Complete(task) },
                                   onLeftSwipe = { pendingAction = TaskAction.Delete(task) },
                                   onSubTaskToggle = { sub -> viewModel.toggleSubTask(sub) },
                                   onEdit = { taskToEdit = task; showEditor = true },
                                   leftActionIcon = Icons.Filled.Delete,
                                   leftActionColor = MaterialTheme.colorScheme.error,
                                   rightActionIcon = Icons.Filled.Check,
                                   rightActionColor = Color(0xFF4CAF50)
                               )
                           }
                       }
                       
                       // COMPLETED
                       if (completedTasks.isNotEmpty()) {
                           item { 
                               Divider(modifier = Modifier.padding(vertical = 8.dp))
                               Text("Completed", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) 
                           }
                           items(items = completedTasks, key = { it.first.id }) { (task, subtasks) ->
                               SwipeableTaskItem(
                                   task = task,
                                   subtasks = subtasks,
                                   isCompletedGroup = true,
                                   onRightSwipe = { pendingAction = TaskAction.UnComplete(task) },
                                   onLeftSwipe = { pendingAction = TaskAction.Delete(task) },
                                   onSubTaskToggle = { sub -> viewModel.toggleSubTask(sub) },
                                   onEdit = { taskToEdit = task; showEditor = true },
                                   leftActionIcon = Icons.Filled.Delete,
                                   leftActionColor = MaterialTheme.colorScheme.error,
                                   rightActionIcon = Icons.Filled.Refresh,
                                   rightActionColor = Color(0xFF2196F3) // Blue for restore/uncomplete
                               )
                           }
                       }
                       
                       // DELETED (TRASH)
                       if (deletedTasks.isNotEmpty()) {
                            item { 
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("Trash", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error) 
                           }
                           items(items = deletedTasks, key = { it.first.id }) { (task, subtasks) ->
                               SwipeableTaskItem(
                                   task = task,
                                   subtasks = subtasks,
                                   isCompletedGroup = false,
                                   onRightSwipe = { pendingAction = TaskAction.Restore(task) },
                                   onLeftSwipe = { pendingAction = TaskAction.PermanentDelete(task) },
                                   onSubTaskToggle = { /* Disabled in Trash */ },
                                   onEdit = { /* Disabled in Trash */ },
                                   leftActionIcon = Icons.Filled.Delete,
                                   leftActionColor = MaterialTheme.colorScheme.error,
                                   rightActionIcon = Icons.Filled.Refresh,
                                   rightActionColor = Color(0xFF4CAF50)
                               )
                           }
                       }
                       
                       if (tasks.isEmpty()) {
                           item {
                                com.shintopc.lifeorganiser.ui.components.EmptyState(
                                    message = "No tasks yet. Tap + to create one!",
                                    icon = Icons.Default.Check,
                                    showArrow = true
                                )
                           }
                       }
                   }
               } else {
                   TasksCalendarScreen(tasks = tasks.map { it.first }, onTaskClick = { task ->
                        taskToEdit = task
                        showEditor = true
                    })
               }
            }
        }
    }
    }
}

@Composable
fun TipCard(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tip: Swipe right \u2192 to Complete/Restore, left \u2190 to Delete.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
    }
}

sealed class TaskAction {
    data class Complete(val task: Task) : TaskAction()
    data class UnComplete(val task: Task) : TaskAction()
    data class Delete(val task: Task) : TaskAction() 
    data class Restore(val task: Task) : TaskAction()
    data class PermanentDelete(val task: Task) : TaskAction()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskItem(
    task: Task,
    subtasks: List<SubTask>,
    isCompletedGroup: Boolean = false,
    onRightSwipe: () -> Unit,
    onLeftSwipe: () -> Unit,
    onSubTaskToggle: (SubTask) -> Unit,
    onEdit: () -> Unit,
    leftActionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    leftActionColor: Color,
    rightActionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    rightActionColor: Color
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            when (it) {
                DismissValue.DismissedToEnd -> {
                    onRightSwipe()
                    // Return false to prevent immediate dismissal animation before dialog confirms
                    // Ideally we want to let it snap back.
                    // If we return true, it disappears.
                    // But we want a dialog.
                    // If we return false, it snaps back.
                    false 
                }
                DismissValue.DismissedToStart -> {
                    onLeftSwipe()
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.25f }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val color = when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> rightActionColor
                DismissDirection.EndToStart -> leftActionColor
                else -> Color.Transparent
            }
            
            val alignment = when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }
            
            val icon = when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> rightActionIcon
                DismissDirection.EndToStart -> leftActionIcon
                else -> Icons.Default.Add // Placeholder
            }
            
            // Animate only if target is not Default
            val scale by androidx.compose.animation.core.animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f, label = "scale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp) 
                    .background(color, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.scale(scale)
                )
            }
        },
        dismissContent = {
             TaskItemContent(
                 task = task, 
                 subtasks = subtasks, 
                 isCompletedGroup = isCompletedGroup, 
                 onSubTaskToggle = onSubTaskToggle, 
                 onEdit = onEdit
             )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemContent(
    task: Task,
    subtasks: List<SubTask>,
    isCompletedGroup: Boolean,
    onSubTaskToggle: (SubTask) -> Unit,
    onEdit: () -> Unit
) {
    val isOverdue = task.dueDate != null && task.dueDate < System.currentTimeMillis() && !task.isCompleted && !task.isDeleted

    Surface(
        shape = RoundedCornerShape(16.dp), 
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f)),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit) // Make whole item clickable for edit
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main Row: Priority + Title
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Priority Indicator
                Box(
                     modifier = Modifier
                         .padding(top = 6.dp, end = 12.dp)
                         .size(10.dp)
                         .clip(CircleShape)
                         .background(
                             when (task.priority) {
                                  TaskPriority.HIGH -> EmergencyAccent
                                  TaskPriority.MEDIUM -> ReminderAccent
                                  TaskPriority.LOW -> TaskAccent
                              }
                         )
                 )

                // Title Section
                Column(modifier = Modifier.weight(1f)) {
                     Text(
                         text = task.title,
                         fontSize = 18.sp,
                         fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.SemiBold,
                         color = if (isOverdue) MaterialTheme.colorScheme.error else if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface,
                         style = if (task.isCompleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                     )

                     if (task.description.isNotBlank()) {
                         Spacer(modifier = Modifier.height(4.dp))
                         Text(
                             text = task.description,
                             fontSize = 14.sp,
                             color = MaterialTheme.colorScheme.onSurfaceVariant,
                             maxLines = 4
                         )
                     }
                     
                     // Date Display
                     if (task.dueDate != null) {
                         Spacer(modifier = Modifier.height(4.dp))
                         
                         // Relative Date
                         val relativeTime = com.shintopc.lifeorganiser.util.DateUtils.getRelativeTimeString(task.dueDate)
                         Text(
                             text = relativeTime,
                             fontWeight = FontWeight.Medium,
                             fontSize = 14.sp,
                             color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                         )

                         // Absolute Timestamp
                         val dateTime = java.time.Instant.ofEpochMilli(task.dueDate).atZone(java.time.ZoneId.systemDefault())
                         val timeStr = if(task.dueTime != null) {
                             val time = java.time.Instant.ofEpochMilli(task.dueTime).atZone(java.time.ZoneId.systemDefault())
                             java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(time)
                         } else ""
                         
                         val dateStr = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy").format(dateTime)
                         
                         Text(
                             text = "$dateStr $timeStr".trim(),
                             style = MaterialTheme.typography.bodySmall,
                             color = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha=0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                         )
                     }
                }
                
                // Actions (Edit only) - Removed, whole item is clickable
            }

            // Subtasks Section (Inline)
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                
                subtasks.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement provides false) {
                              Checkbox(
                                  checked = sub.isCompleted,
                                  onCheckedChange = { onSubTaskToggle(sub) },
                                  modifier = Modifier.scale(0.8f),
                                  colors = CheckboxDefaults.colors(checkedColor = TaskAccent)
                              )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sub.title,
                            fontSize = 14.sp,
                            color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f) else MaterialTheme.colorScheme.onSurface,
                            style = if (sub.isCompleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    task: Task?,
    categories: List<TaskCategory> = emptyList(),
    initialSubtasks: List<SubTask> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (String, String, TaskPriority, Long?, Long?, Boolean, Boolean, List<SubTask>) -> Unit,
    onAddCategory: (String, String) -> Unit = { _, _ -> }
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var desc by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    
    // New Fields
    var dueDate by remember { mutableStateOf(task?.dueDate) }
    var dueTime by remember { mutableStateOf(task?.dueTime) }
    var isReminderOn by remember { mutableStateOf(task?.isReminderOn ?: false) }
    var isAlarm by remember { mutableStateOf(task?.isAlarm ?: false) }
    
    // Subtasks State (copy of initial to allow editing)
    var subtasks by remember { mutableStateOf(initialSubtasks) }
    // Update subtasks if initial changes (e.g. loaded from DB) - but care for loops
    LaunchedEffect(initialSubtasks) {
        if (task != null && subtasks.isEmpty() && initialSubtasks.isNotEmpty()) {
             subtasks = initialSubtasks
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }


    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
         val timePickerState = rememberTimePickerState(
             initialHour = if (dueTime != null) java.time.Instant.ofEpochMilli(dueTime!!).atZone(java.time.ZoneId.systemDefault()).hour else 9,
             initialMinute = if (dueTime != null) java.time.Instant.ofEpochMilli(dueTime!!).atZone(java.time.ZoneId.systemDefault()).minute else 0,
             is24Hour = false
         )
         AlertDialog(
             onDismissRequest = { showTimePicker = false },
             confirmButton = {
                 TextButton(onClick = {
                     val cal = java.util.Calendar.getInstance()
                     cal.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                     cal.set(java.util.Calendar.MINUTE, timePickerState.minute)
                     dueTime = cal.timeInMillis
                     showTimePicker = false
                 }) { Text("OK") }
             },
             dismissButton = {
                 TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
             },
             text = {
                 TimePicker(state = timePickerState)
             }
         )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (task == null) "New Task" else "Edit Task", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Divider()

            // DETAILS SECTION
            Text("Details", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            // PRIORITY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { p ->
                     val isSelected = priority == p
                     FilterChip(
                        selected = isSelected,
                        onClick = { priority = p },
                        label = { Text(p.name) },
                         colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when(p) {
                                TaskPriority.HIGH -> Color(0xFFFF8A80)
                                TaskPriority.MEDIUM -> Color(0xFFFFD180)
                                TaskPriority.LOW -> Color(0xFFB9F6CA)
                            }.copy(alpha = 0.3f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // DATE & TIME ROW
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Date
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Due Date", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (dueDate != null) 
                                java.time.Instant.ofEpochMilli(dueDate!!).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString() 
                                else "Set Date",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Time
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Due Time", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (dueTime != null) 
                                DateTimeFormatter.ofPattern("h:mm a").format(
                                    java.time.Instant.ofEpochMilli(dueTime!!).atZone(java.time.ZoneId.systemDefault())
                                )
                                else "Set Time",
                            style = MaterialTheme.typography.bodyMedium,
                             fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            

            // --- SUBTASKS SECTION ---
            Text("Subtasks", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            
            // Existing Subtasks List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subtasks.forEachIndexed { index, sub ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sub.isCompleted,
                                onCheckedChange = { isChecked ->
                                    val updated = subtasks.toMutableList()
                                    updated[index] = sub.copy(isCompleted = isChecked)
                                    subtasks = updated
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.scale(0.8f)
                            )
                            
                            BasicTextField(
                                value = sub.title,
                                onValueChange = { newTitle ->
                                    val updated = subtasks.toMutableList()
                                    updated[index] = sub.copy(title = newTitle)
                                    subtasks = updated
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (sub.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                ),
                                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                            )
                            
                            IconButton(onClick = {
                                val updated = subtasks.toMutableList()
                                updated.removeAt(index)
                                subtasks = updated
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Add New Subtask Input
            var newSubtaskText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    placeholder = { Text("Add new subtask...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (newSubtaskText.isNotBlank()) {
                            IconButton(onClick = {
                                val updated = subtasks.toMutableList()
                                updated.add(SubTask(taskId = 0, title = newSubtaskText))
                                subtasks = updated
                                newSubtaskText = ""
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
                        if (newSubtaskText.isNotBlank()) {
                            val updated = subtasks.toMutableList()
                            updated.add(SubTask(taskId = 0, title = newSubtaskText))
                            subtasks = updated
                            newSubtaskText = ""
                        }
                    })
                )
            }
            
            Divider()


            // REMINDER TOGGLE
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.material.icons.Icons.Default.Notifications, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remind Me")
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         if (isReminderOn) {
                            val timeText = if (dueTime != null) {
                                 DateTimeFormatter.ofPattern("h:mm a").format(
                                    java.time.Instant.ofEpochMilli(dueTime!!).atZone(java.time.ZoneId.systemDefault())
                                )
                            } else "Set Time"
                            
                            TextButton(onClick = { showTimePicker = true }) {
                                Text(timeText)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                         }

                        Switch(
                            checked = isReminderOn,
                            onCheckedChange = { 
                                isReminderOn = it 
                                if (it && dueTime == null) {
                                    showTimePicker = true
                                }
                            }
                        )
                    }
                }
                
                if (isReminderOn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        val types = listOf(false to "NOTIFICATION", true to "ALARM")
                        types.forEach { (alarmVal, label) ->
                            val selected = isAlarm == alarmVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { isAlarm = alarmVal }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (title.isNotBlank()) onSave(title, desc, priority, dueDate, dueTime, isReminderOn, isAlarm, subtasks) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
