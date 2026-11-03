package com.shintopc.lifeorganiser.data.model

import java.util.UUID

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val repeatMode: String = "NONE",
    val isReminderOn: Boolean = false,
    val isAlarm: Boolean = false,
    val categoryId: Long? = null,
    val totalSubtasks: Int = 0,
    val completedSubtasks: Int = 0,
    val isDeleted: Boolean = false
)
