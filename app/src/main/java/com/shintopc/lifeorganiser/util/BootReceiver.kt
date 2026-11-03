package com.shintopc.lifeorganiser.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shintopc.lifeorganiser.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val reminderDao = database.reminderDao()
                    val activeReminders = reminderDao.getActiveReminders(System.currentTimeMillis())
                    
                    activeReminders.forEach { reminder ->
                        NotificationHelper.scheduleNotification(context, reminder)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
