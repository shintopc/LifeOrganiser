package com.shintopc.lifeorganiser.data.model

import com.shintopc.lifeorganiser.data.local.*

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val tasks: List<TaskEntity> = emptyList(),
    val financeEntries: List<FinanceEntry> = emptyList(),
    val debts: List<FinanceDebt> = emptyList(),
    val categories: List<FinanceCategory> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val passwords: List<PasswordEntity> = emptyList(),
    val documents: List<DocumentEntity> = emptyList(),
    val emergencyContacts: List<EmergencyContactEntity> = emptyList()
)
