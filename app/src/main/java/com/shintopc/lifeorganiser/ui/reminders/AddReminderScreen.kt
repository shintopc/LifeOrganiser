package com.shintopc.lifeorganiser.ui.reminders

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shintopc.lifeorganiser.data.local.ReminderEntity
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    remindersViewModel: RemindersViewModel = viewModel(),
    initialDate: LocalDate? = null,
    reminderToEdit: ReminderEntity? = null,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var title by remember { mutableStateOf(reminderToEdit?.title ?: "") }
    var description by remember { mutableStateOf(reminderToEdit?.description ?: "") }
    
    // Convert timestamp to LocalDate/Time if editing
    val editDateTime = remember(reminderToEdit) {
        reminderToEdit?.let { 
             java.time.Instant.ofEpochMilli(it.dateTime).atZone(ZoneId.systemDefault())
        }
    }
    
    var selectedDate by remember { mutableStateOf(editDateTime?.toLocalDate() ?: initialDate ?: LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(editDateTime?.toLocalTime() ?: java.time.LocalTime.now()) }
    
    var selectedCategory by remember { mutableStateOf(reminderToEdit?.type ?: "General") }
    var repeatMode by remember { mutableStateOf(reminderToEdit?.repeatMode ?: "NONE") }      
    var isAlarm by remember { mutableStateOf(reminderToEdit?.isAlarm ?: false) }
    
    // Update local variables for categories (moved down)

    val categories = listOf(
        "Birthday" to Color(0xFFFF4081), // Pink
        "Anniversary" to Color(0xFFFF9800), // Orange
        "Work" to Color(0xFF2196F3), // Blue
        "General" to Color(0xFF4CAF50) // Green
    )

    val context = LocalContext.current

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    // Auto set recurring for bday/anniversary
    LaunchedEffect(selectedCategory) {
        if (reminderToEdit == null && (selectedCategory == "Birthday" || selectedCategory == "Anniversary")) {
            repeatMode = "YEARLY"
        }
    }

    // Permission Launcher
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
               // Show snackbar or explanation?
            }
        }
    )

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text(if (reminderToEdit != null) "Edit Reminder" else "Add Reminder", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Category Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categories) { (name, color) ->
                        val isSelected = selectedCategory == name
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = name },
                            label = { Text(name) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            },
                             colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = color
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }



            val timePickerDialog = android.app.TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    selectedTime = java.time.LocalTime.of(hourOfDay, minute)
                },
                selectedTime.hour,
                selectedTime.minute,
                false // 12 hour format? Using false = 24h, true = 24h. Actually false is 12h view usually? 
                // Docs: is24HourView. Let's stick to system default or false (AM/PM).
            )

    // ... (Existing code) ...

            // Date Picker (Existing)
            // Date Picker (Existing)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Date & Time", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Date Field
                    OutlinedTextField(
                        value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    // Time Field
                    OutlinedTextField(
                        value = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Time", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Inputs
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )
            
            // Repetition Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repetition", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val modes = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY")
                    items(modes) { mode ->
                         val isSelected = repeatMode == mode
                         FilterChip(
                            selected = isSelected,
                            onClick = { repeatMode = mode },
                            label = { 
                                Text(
                                    mode.lowercase(java.util.Locale.getDefault())
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                             border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                         )
                    }
                }
            }
            
            // Alarm Toggle (New)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAlarm = !isAlarm }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isAlarm, onCheckedChange = { isAlarm = it })
                Text("Alarm Mode (Continuous Sound)", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        val color = categories.find { it.first == selectedCategory }?.second?.toArgb()?.toLong() ?: 0xFF4CAF50
                        
                        // Combine Date and Time
                        val dateTime = java.time.LocalDateTime.of(selectedDate, selectedTime)
                        val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        val reminder = ReminderEntity(
                            id = reminderToEdit?.id ?: 0,
                            title = title,
                            description = description,
                            dateTime = timestamp,
                            type = selectedCategory,
                            color = color.toLong(),
                            repeatMode = repeatMode,
                            isAlarm = isAlarm
                        )
                        remindersViewModel.addReminder(context, reminder)
                        onSaveSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (reminderToEdit != null) "Update Reminder" else "Save Reminder", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
