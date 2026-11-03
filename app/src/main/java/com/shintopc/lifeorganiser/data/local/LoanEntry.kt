package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loan_entries")
data class LoanEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val type: String, // "LOAN" or "CREDIT_CARD"
    val totalAmount: Double, // Optional for credit card, so can be 0.0 or nullable logic handled in VM
    val emiAmount: Double,
    val dueDay: Int,
    val startDate: Long,
    val endDate: Long?,
    val interestRate: Double?,
    val reminderEnabled: Boolean,
    val reminderType: String = "NOTIFICATION", // "NOTIFICATION" or "ALARM"
    val reminderOffset: Int = 0, // Days before the due date to remind (0 = On Due Day)
    val status: String // "ACTIVE" or "CLOSED"
)
