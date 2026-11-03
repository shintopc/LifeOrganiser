package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.ReminderDao
import com.shintopc.lifeorganiser.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemindersRepository @Inject constructor(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun addReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }
}
