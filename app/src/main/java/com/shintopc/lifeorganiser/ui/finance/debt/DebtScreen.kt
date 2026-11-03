package com.shintopc.lifeorganiser.ui.finance.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.hilt.navigation.compose.hiltViewModel
import com.shintopc.lifeorganiser.data.local.FinanceDebt
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import com.shintopc.lifeorganiser.ui.theme.FinanceYellow
import com.shintopc.lifeorganiser.ui.theme.White
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDebtsScreen(
    onBack: () -> Unit,
    viewModel: DebtViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Filters
    var timeFilter by remember { mutableStateOf("Today") } // Today, Weekly, Monthly, Custom, All
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, OPEN, CLOSED
    
    // Custom Date Range
    var customStartDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var customEndDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showCustomRangeDialog by remember { mutableStateOf(false) }
    
    // Inner pickers for Custom Range
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    var entryToEdit by remember { mutableStateOf<FinanceDebt?>(null) }
    var debtToClose by remember { mutableStateOf<FinanceDebt?>(null) }

    var debtToDelete by remember { mutableStateOf<FinanceDebt?>(null) }

    if (debtToDelete != null) {
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("Delete Debt?") },
            text = { Text("Are you sure you want to delete this debt permanently?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        debtToDelete?.let { viewModel.deleteDebt(it) }
                        debtToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (debtToClose != null) {
        AlertDialog(
            onDismissRequest = { debtToClose = null },
            title = { Text("Close Debt?") },
            text = { Text("Mark '${debtToClose?.personName}' as closed (paid)?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        debtToClose?.let { viewModel.closeDebt(it) }
                        debtToClose = null
                    }
                ) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToClose = null }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text("Debts & Lending", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = BluePrimary,
                    contentColor = White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Debt")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Summary Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DebtSummaryCard(
                        title = "I GAVE",
                        todayTotal = state.todayGiven,
                        monthTotal = state.monthGiven,
                        color = Color(0xFF4CAF50), // Green
                        icon = Icons.Default.KeyboardArrowUp,
                        modifier = Modifier.weight(1f)
                    )
                    DebtSummaryCard(
                        title = "I TOOK",
                        todayTotal = state.todayTook,
                        monthTotal = state.monthTook,
                        color = Color(0xFFFF5252), // Red
                        icon = Icons.Default.KeyboardArrowDown,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Time Filter Chips
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Time Range", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Today", "Weekly", "Monthly", "Custom").forEach { f ->
                            FilterChip(
                                selected = timeFilter == f,
                                onClick = { 
                                    timeFilter = f 
                                    if (f == "Custom") showCustomRangeDialog = true
                                },
                                label = { Text(f) },
                                leadingIcon = if (timeFilter == f) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Chips
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "OPEN", "CLOSED").forEach { s ->
                            FilterChip(
                                selected = statusFilter == s,
                                onClick = { statusFilter = s },
                                label = { Text(s) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List Filter Logic
                val allDebts = (state.givenDebts + state.tookDebts).sortedByDescending { it.date }
                val now = java.time.LocalDate.now()
                val zoneId = java.time.ZoneId.systemDefault()
                
                val filteredDebts = remember(allDebts, timeFilter, statusFilter, customStartDate, customEndDate) {
                    allDebts.filter { debt ->
                        // 1. Time Filter
                        val matchesTime = when (timeFilter) {
                            "Today" -> {
                                val startOfDay = now.atStartOfDay(zoneId).toInstant().toEpochMilli()
                                debt.date >= startOfDay
                            }
                            "Weekly" -> {
                                val startOfWeek = now.minusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
                                debt.date >= startOfWeek
                            }
                            "Monthly" -> {
                                val startOfMonth = now.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                                debt.date >= startOfMonth
                            }
                            "Custom" -> {
                                debt.date >= customStartDate && debt.date <= customEndDate
                            }
                            else -> true
                        }
                        
                        // 2. Status Filter
                        val matchesStatus = when (statusFilter) {
                            "OPEN" -> !debt.isPaid
                            "CLOSED" -> debt.isPaid
                            else -> true
                        }

                        matchesTime && matchesStatus
                    }
                }

                if (filteredDebts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No transactions found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                             if (timeFilter == "Custom") {
                                Text(
                                    text = "${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(customStartDate))} - ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(customEndDate))}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredDebts) { debt ->
                            DebtItem(
                                debt = debt,
                                onClick = { entryToEdit = debt },
                                onMarkPaid = { debtToClose = debt }
                            )
                        }
                    }
                }
            }
        }

        // Custom Date Range Dialog
        if (showCustomRangeDialog) {
            AlertDialog(
                onDismissRequest = { showCustomRangeDialog = false },
                title = { Text("Select Range") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showFromPicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("From: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(customStartDate))}")
                        }
                        OutlinedButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("To: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(customEndDate))}")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showCustomRangeDialog = false }) {
                        Text("Apply")
                    }
                }
            )
        }
        
        if (showFromPicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = customStartDate)
            DatePickerDialog(
                onDismissRequest = { showFromPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        customStartDate = datePickerState.selectedDateMillis ?: customStartDate
                        showFromPicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        if (showToPicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = customEndDate)
            DatePickerDialog(
                onDismissRequest = { showToPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = datePickerState.selectedDateMillis ?: customEndDate
                        // Set to End of Day
                        val c = Calendar.getInstance()
                        c.timeInMillis = selected
                        c.set(Calendar.HOUR_OF_DAY, 23)
                        c.set(Calendar.MINUTE, 59)
                        customEndDate = c.timeInMillis
                        
                        showToPicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showAddDialog || entryToEdit != null) {
            AddDebtOverlay(
                viewModel = viewModel,
                debtToEdit = entryToEdit,
                onDismiss = {
                    showAddDialog = false
                    entryToEdit = null
                },
                onDelete = {
                    if (entryToEdit != null) {
                         debtToDelete = entryToEdit
                         entryToEdit = null
                    }
                }
            )
        }
    }
}

@Composable
fun DebtSummaryCard(
    title: String,
    todayTotal: Double,
    monthTotal: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            Column {
                Text("Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${String.format("%.0f", todayTotal)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Column {
                Text("This Month", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${String.format("%.0f", monthTotal)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DebtItem(
    debt: FinanceDebt,
    onClick: () -> Unit,
    onMarkPaid: () -> Unit,
    showCloseButton: Boolean = true // Added parameter
) {
    val isGiven = debt.type == "GAVE"
    val color = if (isGiven) Color(0xFF4CAF50) else Color(0xFFFF5252)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = debt.personName.take(1).uppercase(java.util.Locale.getDefault()),
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(debt.personName, fontWeight = FontWeight.Bold)
                Text(
                    if (isGiven) "You gave" else "You borrowed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (debt.expectedDate != null && !debt.isPaid) {
                     Text(
                        "Due: ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(debt.expectedDate))}",
                        fontSize = 10.sp,
                        color = Color.Red
                     )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${String.format("%.0f", debt.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (!debt.isPaid) {
                    if (showCloseButton) { // Control visibility
                        TextButton(onClick = onMarkPaid) {
                            Text("CLOSE", fontSize = 10.sp)
                        }
                    }
                } else {
                    Text("CLOSED", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtOverlay(
    viewModel: DebtViewModel,
    debtToEdit: FinanceDebt? = null,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var type by remember { mutableStateOf(debtToEdit?.type ?: "GAVE") } // GAVE or TOOK
    var personName by remember { mutableStateOf(debtToEdit?.personName ?: "") }
    var amount by remember { mutableStateOf(debtToEdit?.amount?.toString()?.replace(".0", "") ?: "") }
    var note by remember { mutableStateOf(debtToEdit?.note ?: "") }
    
    var reminderEnabled by remember { mutableStateOf(debtToEdit?.reminderEnabled ?: false) }
    var reminderType by remember { mutableStateOf(debtToEdit?.reminderType ?: "NOTIFICATION") }
    
    // Dates
    var date by remember { mutableStateOf(debtToEdit?.date ?: System.currentTimeMillis()) }
    var expectedDate by remember { mutableStateOf(debtToEdit?.expectedDate) }
    
    // Reminder Date & Time
    // Initialize reminder parts from existing reminderDate if available, else null/defaults
    var reminderDateMillis by remember { mutableStateOf(debtToEdit?.reminderDate) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf("DATE") } // DATE, EXPECTED, REMINDER

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(16.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
            .widthIn(max = 400.dp)
            .fillMaxWidth() // Center it usually
    ) {
        Column(
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(if (debtToEdit == null) "Add Debt/Loan" else "Edit Entry", style = MaterialTheme.typography.headlineSmall)

            // Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf("GAVE", "TOOK").forEach { t ->
                    val selected = type == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) if (t == "GAVE") Color(0xFF4CAF50) else Color(0xFFFF5252) else Color.Transparent)
                            .clickable { type = t }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (t == "GAVE") "I GAVE" else "I TOOK",
                            color = if (selected) White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text("Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Date Selection
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { 
                        datePickerTarget = "DATE"
                        showDatePicker = true 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(date)))
                }
                OutlinedButton(
                    onClick = {
                        datePickerTarget = "EXPECTED"
                        showDatePicker = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (expectedDate != null) SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(expectedDate!!)) else "Due Date (Opt)")
                }
            }

            // Reminder UI - Switch
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Remind Me", style = MaterialTheme.typography.bodyLarge)
                
                Switch(
                     checked = reminderEnabled,
                     onCheckedChange = { 
                         reminderEnabled = it
                         if (it && reminderDateMillis == null) {
                              datePickerTarget = "REMINDER"
                              showDatePicker = true
                         }
                     }
                )
            }
            
            if (reminderEnabled) {
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     // Reminder Date Button
                    OutlinedButton(
                        onClick = { 
                            datePickerTarget = "REMINDER"
                            showDatePicker = true 
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        val dateText = if (reminderDateMillis != null) SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(reminderDateMillis!!)) else "Date"
                        Text(dateText)
                    }
                    
                    // Reminder Time Button
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                         val timeText = if (reminderDateMillis != null) {
                             java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(
                                java.time.Instant.ofEpochMilli(reminderDateMillis!!).atZone(java.time.ZoneId.systemDefault())
                            )
                        } else "Time"
                        Text(timeText)
                    }
                 }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Reminder Type Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    val types = listOf("NOTIFICATION", "ALARM")
                    types.forEach { t ->
                        val selected = reminderType == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { reminderType = t }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                             Text(
                                text = t,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (debtToEdit != null && onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (personName.isNotBlank() && amount.toDoubleOrNull() != null) {
                        if (debtToEdit != null) {
                            viewModel.updateDebt(
                                context,
                                debtToEdit.copy(
                                    personName = personName,
                                    amount = amount.toDouble(),
                                    type = type,
                                    date = date,
                                    expectedDate = expectedDate,
                                    note = note,
                                    reminderEnabled = reminderEnabled,
                                    reminderType = reminderType,
                                    reminderDate = if (reminderEnabled) reminderDateMillis else null
                                )
                            )
                        } else {
                            viewModel.addDebt(
                                context,
                                personName = personName,
                                amount = amount,
                                type = type,
                                date = date,
                                expectedDate = expectedDate,
                                note = note,
                                reminderEnabled = reminderEnabled,
                                reminderType = reminderType,
                                reminderDate = if (reminderEnabled) reminderDateMillis else null
                            )
                        }
                        onDismiss()
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
    
    if (showDatePicker) {
        val initialDate = when(datePickerTarget) {
            "DATE" -> date
            "EXPECTED" -> expectedDate ?: System.currentTimeMillis()
            "REMINDER" -> reminderDateMillis ?: System.currentTimeMillis()
            else -> System.currentTimeMillis()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val sel = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    when(datePickerTarget) {
                        "DATE" -> date = sel
                        "EXPECTED" -> expectedDate = sel
                        "REMINDER" -> {
                            // Preserve time if modifying date, or default to 9 AM
                            val current = if (reminderDateMillis != null) {
                                java.time.Instant.ofEpochMilli(reminderDateMillis!!).atZone(java.time.ZoneId.systemDefault())
                            } else null
                            
                            val newDate = java.time.Instant.ofEpochMilli(sel).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            val time = current?.toLocalTime() ?: java.time.LocalTime.of(9, 0)
                            
                            reminderDateMillis = java.time.LocalDateTime.of(newDate, time)
                                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                                
                            // Auto open time picker if setting reminder for first time
                            if (current == null) showTimePicker = true
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker
    if (showTimePicker) {
         val timePickerState = rememberTimePickerState(
             initialHour = if (reminderDateMillis != null) java.time.Instant.ofEpochMilli(reminderDateMillis!!).atZone(java.time.ZoneId.systemDefault()).hour else 9,
             initialMinute = if (reminderDateMillis != null) java.time.Instant.ofEpochMilli(reminderDateMillis!!).atZone(java.time.ZoneId.systemDefault()).minute else 0,
             is24Hour = false
         )
         AlertDialog(
             onDismissRequest = { showTimePicker = false },
             confirmButton = {
                 TextButton(onClick = {
                     val cal = java.util.Calendar.getInstance()
                     // If we have a date set, use it. Else use today.
                     if (reminderDateMillis != null) {
                         cal.timeInMillis = reminderDateMillis!!
                     }
                     cal.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                     cal.set(java.util.Calendar.MINUTE, timePickerState.minute)
                     reminderDateMillis = cal.timeInMillis
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
}
