package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val dateTime: Long, // Timestamp of the event
    val type: String, // "Birthday", "Anniversary", "General", "Work"
    val color: Long, // Color ARGB
    val repeatMode: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    val isAlarm: Boolean = false // If true, plays insistent alarm sound
)
