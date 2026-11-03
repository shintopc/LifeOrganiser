package com.shintopc.lifeorganiser.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.shintopc.lifeorganiser.data.local.FinanceEntry
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceReportScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    val monthEntries by viewModel.getEntriesForMonth(selectedYear, selectedMonth).collectAsState(initial = emptyList())

    val totalIncome = monthEntries.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = monthEntries.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    // Group by Category
    val expenseByCategory = monthEntries
        .filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { entry -> entry.amount } }
        .toList()
        .sortedByDescending { it.second }

    val incomeByCategory = monthEntries
        .filter { it.type == "INCOME" }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { entry -> entry.amount } }
        .toList()
        .sortedByDescending { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Report", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Month Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (selectedMonth == 0) {
                        selectedMonth = 11
                        selectedYear--
                    } else {
                        selectedMonth--
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSurface)
                }

                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }.time
                )
                Text(monthName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

                IconButton(onClick = {
                    if (selectedMonth == 11) {
                        selectedMonth = 0
                        selectedYear++
                    } else {
                        selectedMonth++
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Income", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text("₹${String.format("%.0f", totalIncome)}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Expense", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text("₹${String.format("%.0f", totalExpense)}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Balance", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text("₹${String.format("%.0f", balance)}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // --- Income Breakdown ---
                item {
                    if (totalIncome > 0) {
                        Text("Income Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                item {
                    if (totalIncome > 0) {
                        PieChart(
                            data = incomeByCategory.map { it.first to it.second },
                            total = totalIncome,
                            isIncome = true
                        )
                    }
                }

                items(incomeByCategory) { (category, amount) ->
                    val percentage = if (totalIncome > 0) amount / totalIncome else 0.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(category, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                                Text("₹${String.format("%.0f", amount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = percentage.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF4CAF50), // Green for income
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // --- Expense Breakdown ---
                item {
                    if (totalExpense > 0) {
                        Text("Expense Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                item {
                    if (totalExpense > 0) {
                        PieChart(
                            data = expenseByCategory.map { it.first to it.second },
                            total = totalExpense,
                            isIncome = false
                        )
                    }
                }

                items(expenseByCategory) { (category, amount) ->
                    val percentage = if (totalExpense > 0) amount / totalExpense else 0.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(category, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                                Text("₹${String.format("%.0f", amount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = percentage.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (percentage > 0.5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
                
                if (expenseByCategory.isEmpty() && incomeByCategory.isEmpty()) {
                    item {
                         Text("No transactions recorded for this month.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun PieChart(
    data: List<Pair<String, Double>>,
    total: Double,
    isIncome: Boolean
) {
    val colors = listOf(
        Color(0xFFEF5350), // Red 400
        Color(0xFF42A5F5), // Blue 400
        Color(0xFF66BB6A), // Green 400
        Color(0xFFFFA726), // Orange 400
        Color(0xFFAB47BC), // Purple 400
        Color(0xFF26C6DA), // Cyan 400
        Color(0xFFFF7043), // Deep Orange 400
        Color(0xFF78909C)  // Blue Grey 400
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                data.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.second / total * 360).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }
            // Optional: Inner circle for Donut chart look
             val innerColor = MaterialTheme.colorScheme.background
             androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
                drawCircle(color = innerColor)
             }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legend (FlowRow would be better but requires ExperimentalLayout, using simplified Column/Row for now)
        Column {
            data.forEachIndexed { index, entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colors[index % colors.size])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${entry.first} (${String.format("%.0f", entry.second)})",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
