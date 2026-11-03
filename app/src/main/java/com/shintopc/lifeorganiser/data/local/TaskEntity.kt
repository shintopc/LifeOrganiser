package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.shintopc.lifeorganiser.data.model.TaskPriority

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: TaskPriority,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val repeatMode: String = "NONE",
    val isReminderOn: Boolean = false,
    val isAlarm: Boolean = false,
    val categoryId: Long? = null,
    val isDeleted: Boolean = false
)
