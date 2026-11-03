package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val color: Int, // ARGB or Color Resource ID
    val isPinned: Boolean = false,
    val timestamp: Long
)
