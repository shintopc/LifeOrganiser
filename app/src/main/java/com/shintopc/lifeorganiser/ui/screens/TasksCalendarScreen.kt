package com.shintopc.lifeorganiser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.data.model.Task
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalTextStyle

@Composable
fun TasksCalendarScreen(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Month Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Prev Month")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        // Days of Week Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // Sun=0? java.time DayOfWeek Mon=1..Sun=7. 
        // Need to check standard java.time DayOfWeek. 1=Mon, 7=Sun.
        // If we want Sun start: Mon(1)=>1, Sun(7)=>0? No.
        // Let's assume Sun start.
        // If 1st is Mon (1), offset should be 1. Sun (7) offset 0.
        // Wait, normally Sun is 0 in Grid.
        // If 1st is Sun (7), offset is 0.
        // If 1st is Mon (1), offset is 1.
        val startOffset = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value

        val totalCells = daysInMonth + startOffset
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp) // Fixed height for calendar part
        ) {
            items(totalCells) { index ->
                if (index < startOffset) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val day = index - startOffset + 1
                    val date = currentMonth.atDay(day)
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    
                    // Filter tasks for this date
                    val tasksForDay = tasks.filter { task ->
                        if (task.dueDate != null) {
                            val taskDate = java.time.Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                            taskDate == date
                        } else false
                    }
                    val hasTasks = tasksForDay.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { selectedDate = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (hasTasks) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Divider()
        
        // Tasks List for Selected Date
        Text(
            text = "Tasks for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        val selectedTasks = tasks.filter { task ->
            if (task.dueDate != null) {
                val taskDate = java.time.Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                taskDate == selectedDate
            } else false
        }
        
        if (selectedTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No tasks for this day", color = Color.Gray)
            }
        } else {
             // Reusing TaskItem would be ideal, but for now simple list or pass TaskItem
             // I can import TaskItem
             // Let's just list titles for brevity in this component, or refactor TaskItem
             // I'll assume simple display for now to verify.
             // Actually, the main screen will handle edit.
             com.shintopc.lifeorganiser.ui.screens.TaskListForCalendar(selectedTasks, onTaskClick)
        }
    }
}

@Composable
fun TaskListForCalendar(tasks: List<Task>, onClick: (Task) -> Unit) {
    Column {
        tasks.forEach { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onClick(task) },
                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                 elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.isCompleted, onCheckedChange = null) // Read only here
                    Text(
                        text = task.title,
                        modifier = Modifier.weight(1f),
                        style = if(task.isCompleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                    )
                }
            }
        }
    }
}
