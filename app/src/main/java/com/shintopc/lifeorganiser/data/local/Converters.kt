package com.shintopc.lifeorganiser.data.local

import androidx.room.TypeConverter
import com.shintopc.lifeorganiser.data.model.TaskPriority

class Converters {
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority {
        return try {
            TaskPriority.valueOf(priority)
        } catch (e: Exception) {
            TaskPriority.MEDIUM
        }
    }
}
