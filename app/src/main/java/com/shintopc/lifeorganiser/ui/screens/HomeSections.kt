package com.shintopc.lifeorganiser.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.ui.theme.EmergencyAccent
import com.shintopc.lifeorganiser.ui.theme.FinanceAccent
import com.shintopc.lifeorganiser.ui.theme.ReminderAccent
import com.shintopc.lifeorganiser.ui.theme.TaskAccent
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.Instant

@Composable
fun CategoryHeader(title: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp, top = 2.dp)
    ) {
        Surface(
            modifier = Modifier.size(26.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FinanceSummaryRow(state: HomeState) {
    val balance = state.todayIncome - state.todayExpense
    val isPositive = balance >= 0

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = FinanceAccent.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = FinanceAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Today's Finances",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPositive) TaskAccent.copy(alpha = 0.15f) else EmergencyAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isPositive) "+ Net Positive" else "Deficit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) TaskAccent else EmergencyAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Equal-Width Metrics Columns: Never overlaps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Net Balance Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${String.format("%,d", balance.toInt())}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // Income Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INCOME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TaskAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "+₹${state.todayIncome.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TaskAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // Expense Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "EXPENSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "-₹${state.todayExpense.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            CategoryHeader(title, icon, color)
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
fun CategorizedTimelineView(
    items: List<TimelineItem>,
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(if (backgroundColor != Color.Transparent) 16.dp else 0.dp)
    ) {
        // 1. Reminders
        val reminders = items.filterIsInstance<TimelineItem.Reminder>()
        if (reminders.isNotEmpty()) {
            CategorySection("Reminders", Icons.Default.Notifications, ReminderAccent) {
                reminders.forEach { CompactItemRow(it) }
            }
        }

        // 2. Bank/EMI
        val loans = items.filterIsInstance<TimelineItem.Loan>()
        if (loans.isNotEmpty()) {
            CategorySection("Bank / EMI", Icons.Default.Home, EmergencyAccent) {
                loans.forEach { CompactItemRow(it) }
            }
        }

        // 3. Tasks
        val tasks = items.filterIsInstance<TimelineItem.Task>()
        if (tasks.isNotEmpty()) {
            CategorySection("Tasks", Icons.Default.CheckCircle, TaskAccent) {
                tasks.forEach { CompactItemRow(it) }
            }
        }

        // 4. Borrow/Lend
        val debts = items.filterIsInstance<TimelineItem.Debt>()
        if (debts.isNotEmpty()) {
            CategorySection("Borrow / Lend", Icons.Default.Star, FinanceAccent) {
                debts.forEach { CompactItemRow(it) }
            }
        }

        if (items.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No items scheduled for this period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CompactItemRow(item: TimelineItem) {
    val date = Instant.ofEpochMilli(item.sortTime).atZone(ZoneId.systemDefault())
    val timeStr = date.format(DateTimeFormatter.ofPattern("h:mm a"))
    val dateDay = date.format(DateTimeFormatter.ofPattern("MMM dd"))

    val (title, color) = when (item) {
        is TimelineItem.Task -> {
            val taskColor = when (item.data.priority) {
                com.shintopc.lifeorganiser.data.model.TaskPriority.HIGH -> EmergencyAccent
                com.shintopc.lifeorganiser.data.model.TaskPriority.MEDIUM -> ReminderAccent
                com.shintopc.lifeorganiser.data.model.TaskPriority.LOW -> TaskAccent
            }
            item.data.title to taskColor
        }
        is TimelineItem.Reminder -> {
            val remColor = if (item.data.color != 0L) Color(item.data.color) else ReminderAccent
            item.data.title to remColor
        }
        is TimelineItem.Debt -> {
            val isBorrow = item.data.type == "TOOK"
            val prefix = if (isBorrow) "Borrowed from" else "Lent to"
            val amountStr = "₹${item.data.amount.toInt()}"
            val debtColor = if (isBorrow) EmergencyAccent else TaskAccent
            "$prefix ${item.data.personName}: $amountStr" to debtColor
        }
        is TimelineItem.Loan -> {
            val amountStr = "₹${item.data.emiAmount.toInt()}"
            val due = Instant.ofEpochMilli(item.dueDate).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM dd"))
            "${item.data.title} (EMI): $amountStr (Due $due)" to EmergencyAccent
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time & Date Column (Fixed width, never overflows)
        Column(modifier = Modifier.width(72.dp)) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateDay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        // Color Indicator Pill
        Surface(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp),
            shape = RoundedCornerShape(2.dp),
            color = color
        ) {}

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
    Divider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

@Composable
fun TimelineList(items: List<TimelineItem>, isToday: Boolean = false) {
    if (items.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items scheduled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column(modifier = Modifier.padding(start = 8.dp)) {
            items.forEachIndexed { index, item ->
                TimelineItemRow(item = item, isLast = index == items.lastIndex, isToday = isToday)
            }
        }
    }
}

@Composable
fun TimelineItemRow(item: TimelineItem, isLast: Boolean, isToday: Boolean) {
    val date = Instant.ofEpochMilli(item.sortTime).atZone(ZoneId.systemDefault())
    val timeStr = date.format(DateTimeFormatter.ofPattern("h:mm a"))
    val dateStr = if (isToday) "Today" else date.format(DateTimeFormatter.ofPattern("MMM dd"))

    val (title, _, color) = when (item) {
        is TimelineItem.Task -> {
            val taskColor = when (item.data.priority) {
                com.shintopc.lifeorganiser.data.model.TaskPriority.HIGH -> EmergencyAccent
                com.shintopc.lifeorganiser.data.model.TaskPriority.MEDIUM -> ReminderAccent
                com.shintopc.lifeorganiser.data.model.TaskPriority.LOW -> TaskAccent
            }
            Triple(item.data.title, Icons.Default.CheckCircle, taskColor)
        }
        is TimelineItem.Reminder -> {
            val remColor = if (item.data.color != 0L) Color(item.data.color) else ReminderAccent
            Triple(item.data.title, Icons.Default.Notifications, remColor)
        }
        is TimelineItem.Debt -> {
            val isBorrow = item.data.type == "TOOK"
            val prefix = if (isBorrow) "Borrowed from" else "Lent to"
            val amountStr = "₹${item.data.amount.toInt()}"
            val debtColor = if (isBorrow) EmergencyAccent else TaskAccent
            Triple("$prefix ${item.data.personName}: $amountStr", Icons.Default.Star, debtColor)
        }
        is TimelineItem.Loan -> {
            val amountStr = "₹${item.data.emiAmount.toInt()}"
            val due = Instant.ofEpochMilli(item.dueDate).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM dd"))
            Triple("${item.data.title} (EMI): $amountStr (Due $due)", Icons.Default.Star, EmergencyAccent)
        }
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline Thread
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(14.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                border = BorderStroke(2.dp, color)
            ) {}
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isToday) timeStr else "$dateStr • $timeStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    itemCheck: Int,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$itemCheck",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}
