package com.shintopc.lifeorganiser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_debts")
data class FinanceDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val amount: Double,
    val type: String, // "GAVE" (I gave/Lent) or "TOOK" (I took/Borrowed)
    val date: Long,
    val expectedDate: Long? = null,
    val note: String = "",
    val isPaid: Boolean = false, // false = OPEN, true = CLOSED
    val reminderEnabled: Boolean = false,
    val reminderType: String = "NOTIFICATION", // "NOTIFICATION" or "ALARM"
    val reminderDate: Long? = null
)
