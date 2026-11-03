package com.shintopc.lifeorganiser.ui.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shintopc.lifeorganiser.data.local.FinanceEntry
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import com.shintopc.lifeorganiser.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.shintopc.lifeorganiser.ui.finance.debt.FinanceDebtsScreen
import com.shintopc.lifeorganiser.ui.finance.LoansScreen

@Composable
fun FinanceFeature(
    onToggleBars: (Boolean) -> Unit,
    financeViewModel: FinanceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf("DASHBOARD") }
    var editingTransaction by remember { mutableStateOf<FinanceEntry?>(null) }

    when (currentScreen) {
        "DASHBOARD" -> {
            FinanceScreen(
                viewModel = financeViewModel,
                onViewReport = { currentScreen = "REPORT" },
                onViewDebts = { currentScreen = "DEBTS" },
                onViewLoans = { currentScreen = "LOANS" },
                onAddTransaction = { 
                    editingTransaction = it
                    currentScreen = "EDITOR" 
                }
            )
        }
        "EDITOR" -> {
            // Screen replacement for editing
            LaunchedEffect(Unit) { onToggleBars(false) }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AddTransactionOverlay(
                    viewModel = financeViewModel,
                    entryToEdit = editingTransaction,
                    onDismiss = { 
                        onToggleBars(true)
                        currentScreen = "DASHBOARD" 
                    }
                )
            }
        }
        "REPORT" -> {
            FinanceReportScreen(
                viewModel = financeViewModel,
                onBack = { currentScreen = "DASHBOARD" }
            )
        }
        "DEBTS" -> {
            FinanceDebtsScreen(
                onBack = { currentScreen = "DASHBOARD" }
            )
        }
        "LOANS" -> {
            LoansScreen(
                onBack = { currentScreen = "DASHBOARD" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
    onViewReport: () -> Unit,
    onViewDebts: () -> Unit,
    onViewLoans: () -> Unit,
    onAddTransaction: (FinanceEntry?) -> Unit
) {
    val transactions by viewModel.recentTransactions.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    
    var isBudgetDialogOpen by remember { mutableStateOf(false) }
    var optionsEntry by remember { mutableStateOf<FinanceEntry?>(null) }
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    // Custom Date Range State
    var customStartDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var customEndDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showCustomRangeDialog by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    // Use a Box to layer the dialog (overlay) on top of the content
    // This avoids creating a new Window, preventing flickering issues on some devices.
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                FloatingActionButton(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onAddTransaction(null)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Summary Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Today",
                            income = todayStats.income,
                            expense = todayStats.expense,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "This Month",
                            income = monthlyStats.income,
                            expense = monthlyStats.expense,
                            budget = monthlyBudget,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onViewReport,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Analysis", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { isBudgetDialogOpen = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Budget", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onViewDebts,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Borrow & Lend", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onViewLoans,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Bank/EMI", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    val currentFilter by viewModel.transactionFilter.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            TransactionFilter.ALL to "All",
                            TransactionFilter.TODAY to "Today",
                            TransactionFilter.WEEKLY to "Weekly",
                            TransactionFilter.MONTHLY to "Monthly",
                            TransactionFilter.CUSTOM to "Custom"
                        ).forEach { (filter, label) ->
                            FilterChip(
                                selected = currentFilter == filter,
                                onClick = { 
                                    if (filter == TransactionFilter.CUSTOM) {
                                        showCustomRangeDialog = true
                                    } else {
                                        viewModel.setTransactionFilter(filter) 
                                    }
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                leadingIcon = if (currentFilter == filter) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }

                item {
                    Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.height(300.dp)) {
                             com.shintopc.lifeorganiser.ui.components.EmptyState(
                                message = "No transactions. Track your income & expenses!",
                                icon = androidx.compose.material.icons.Icons.Default.Star,
                                showArrow = true
                            )
                        }
                    }
                } else {
                    items(transactions) { entry ->
                        TransactionItem(entry = entry, onClick = { optionsEntry = entry })
                    }
                }
                
            }
        }
        
        // --- Budget Dialog ---
        if (isBudgetDialogOpen) {
            var budgetAmount by remember { mutableStateOf(if (monthlyBudget > 0) monthlyBudget.toString() else "") }
            AlertDialog(
                onDismissRequest = { isBudgetDialogOpen = false },
                title = { Text("Set Monthly Budget") },
                text = {
                    Column {
                        Text("Plan your monthly expenses.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = budgetAmount,
                            onValueChange = { budgetAmount = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("Budget Amount") },
                            prefix = { Text("₹") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = budgetAmount.toDoubleOrNull() ?: 0.0
                            viewModel.updateBudget(amt)
                            isBudgetDialogOpen = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isBudgetDialogOpen = false }) { Text("Cancel") }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // --- Options Dialog ---
        if (optionsEntry != null) {
            var showDeleteConfirm by remember { mutableStateOf(false) }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete Transaction?") },
                    text = { Text("Are you sure you want to delete this transaction?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteTransaction(optionsEntry!!)
                                optionsEntry = null
                                showDeleteConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                    }
                )
            } else {
                AlertDialog(
                    onDismissRequest = { optionsEntry = null },
                    title = { Text("Transaction Options") },
                    text = { 
                        Column {
                            Text("Selected: ${optionsEntry?.category}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Amount: ₹${optionsEntry?.amount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onAddTransaction(optionsEntry)
                                optionsEntry = null
                            }
                        ) {
                            Text("Edit")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                    Button(onClick = { 
                        viewModel.setCustomDateRange(customStartDate, customEndDate)
                        viewModel.setTransactionFilter(TransactionFilter.CUSTOM)
                        showCustomRangeDialog = false 
                    }) {
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
                        val c = java.util.Calendar.getInstance()
                        c.timeInMillis = selected
                        c.set(java.util.Calendar.HOUR_OF_DAY, 23)
                        c.set(java.util.Calendar.MINUTE, 59)
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
    }
}

// Renamed from Dialog to Overlay to reflect new usage
@Composable
fun TransactionItem(entry: FinanceEntry, onClick: () -> Unit) {
    val isIncome = entry.type == "INCOME"
    val color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF5252)
    val icon = if (isIncome) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
    val sign = if (isIncome) "+" else "-"

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (entry.note.isNotEmpty()) {
                    Text(entry.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(entry.date)),
                    fontSize = 10.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$sign₹${String.format("%.0f", entry.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// Renamed from Dialog to Overlay to reflect new usage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionOverlay(
    viewModel: FinanceViewModel,
    entryToEdit: FinanceEntry? = null,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(entryToEdit?.type ?: "EXPENSE") }
    var amount by remember { mutableStateOf(entryToEdit?.amount?.toString()?.replace(".0", "") ?: "") }
    var category by remember { mutableStateOf(entryToEdit?.category ?: "") }
    var note by remember { mutableStateOf(entryToEdit?.note ?: "") }
    var selectedDate by remember { mutableStateOf(entryToEdit?.date ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    
    // Only request focus if adding new (otherwise it's annoying when editing)
    LaunchedEffect(Unit) {
        if (entryToEdit == null) {
            focusRequester.requestFocus()
        }
    }

    val categories by viewModel.getCategories(type).collectAsState()

    // Auto select first category
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && category.isEmpty()) {
            category = categories.first().name
        } else if (categories.isNotEmpty() && !categories.any { it.name == category }) {
             category = categories.first().name
        }
    }

    // Request focus on open
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Direct Surface usage without Dialog wrapper
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple
                onClick = {} // Swallow click
            )
            .widthIn(max = 400.dp) // Constrain width on large screens
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val types = listOf("INCOME", "EXPENSE")
                types.forEach { t ->
                    val selected = type == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) if(t=="INCOME") Color(0xFF4CAF50) else Color(0xFFFF5252) else Color.Transparent)
                            .clickable { type = t }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            t,
                            color = if (selected) White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text("Amount") },
                singleLine = true,
                // Use Decimal keyboard
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            // Date Picker Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { showDatePicker = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(selectedDate)),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDate = datePickerState.selectedDateMillis ?: selectedDate
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

            // Category Selection (Horizontal List)
            Column {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                var isAddingCategory by remember { mutableStateOf(false) }
                var newCategoryName by remember { mutableStateOf("") }

                if (isAddingCategory) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            placeholder = { Text("New Category") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCategory(newCategoryName, type)
                                category = newCategoryName // Auto-select the new one
                                isAddingCategory = false
                                newCategoryName = ""
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = BluePrimary)
                        }
                        IconButton(onClick = { isAddingCategory = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray)
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(categories) { cat ->
                            val isSelected = category == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat.name },
                                label = { Text(cat.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (type == "INCOME") Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f),
                                    selectedLabelColor = if (type == "INCOME") Color(0xFF388E3C) else Color(0xFFD32F2F),
                                    selectedLeadingIconColor = if (type == "INCOME") Color(0xFF388E3C) else Color(0xFFD32F2F)
                                )
                            )
                        }
                        item {
                            AssistChip(
                                onClick = { isAddingCategory = true },
                                label = { Text("Add") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = "Add Category", modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.addTransaction(
                                amount = amt,
                                type = type,
                                category = category,
                                note = note,
                                date = selectedDate, 
                                id = entryToEdit?.id ?: 0
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Save")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Composable
private fun UnusedSummaryCard(
    title: String,
    income: Double,
    expense: Double,
    budget: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val balance = income - expense
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "₹${String.format("%.0f", balance)}",
                color = if (balance >= 0) Color(0xFF00E676) else Color(0xFFFF5252),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text("Balance", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            
            if (budget > 0) {
                val progress = (expense / budget).coerceIn(0.0, 1.0).toFloat()
                val isOverBudget = expense > budget
                val isNearBudget = expense > budget * 0.8 && !isOverBudget
                
                Spacer(modifier = Modifier.height(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            isOverBudget -> Color(0xFFFF5252)
                            isNearBudget -> Color(0xFFFFA726)
                            else -> Color(0xFF00E676)
                        },
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOverBudget) "Over Budget!" else if (isNearBudget) "Approaching Limit" else "Budget: ₹${String.format("%.0f", budget)}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) Color(0xFFFF5252) else if (isNearBudget) Color(0xFFFFA726) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isOverBudget) {
                             Icon(
                                Icons.Default.Close, // Or Warning if available
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(10.dp)
                             )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "₹${String.format("%.0f", income)}",
                        color = Color(0xFF00E676),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Income", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))

                // Expense
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "₹${String.format("%.0f", expense)}",
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Expense", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }
            }
        }
    }
}
