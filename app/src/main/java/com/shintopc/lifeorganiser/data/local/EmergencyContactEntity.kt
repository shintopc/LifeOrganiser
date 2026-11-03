package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val number: String, // Can be alphanumeric for some services
    val category: String, // e.g., "Utilities", "Medical", "Banks"
    val note: String = ""
)
