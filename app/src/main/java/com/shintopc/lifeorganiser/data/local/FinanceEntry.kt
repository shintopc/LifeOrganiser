package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_entries")
data class FinanceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long // Timestamp
)
