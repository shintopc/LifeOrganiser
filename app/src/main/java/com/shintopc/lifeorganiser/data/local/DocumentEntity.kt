package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val docNumber: String,
    val docType: String, // Aadhaar, PAN, etc.
    val filePath: String,
    val fileType: String, // JPG, PDF
    val timestamp: Long
)
