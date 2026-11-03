package com.shintopc.lifeorganiser.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shintopc.lifeorganiser.data.local.ReminderEntity

object NotificationHelper {
    const val CHANNEL_ID = "reminder_channel"
    const val CHANNEL_NAME = "Reminders"
    const val ALARM_CHANNEL_ID = "reminder_alarm_channel"
    const val ALARM_CHANNEL_NAME = "Reminder Alarms"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Standard Notification Channel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Channel for Life Organiser reminders"
            }
            
            // Alarm Channel
            val alarmChannel = NotificationChannel(ALARM_CHANNEL_ID, ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for Alarms"
                setSound(
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM),
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    fun scheduleNotification(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("message", reminder.description.ifEmpty { "You have a reminder!" })
            putExtra("id", reminder.id)
            putExtra("isAlarm", reminder.isAlarm)
        }

        // Use a unique request code based on reminder ID
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var timeInMillis = reminder.dateTime
        
        // Handle Repetition Logic
        if (timeInMillis <= System.currentTimeMillis() && reminder.repeatMode != "NONE") {
            val dateTime = java.time.Instant.ofEpochMilli(reminder.dateTime).atZone(java.time.ZoneId.systemDefault())
            val now = java.time.LocalDateTime.now()
            
            // Advance logic: We need to find the *next* occurrence relative to now.
            // If it was due yesterday and repeats daily, it should trigger today (if time passed) or tomorrow?
            // "Next Future Occurrence" logic.
            // Simplified: If past, add interval until in future.
            
            var nextTime = dateTime
            while (nextTime.toInstant().toEpochMilli() <= System.currentTimeMillis()) {
                 nextTime = when (reminder.repeatMode) {
                     "DAILY" -> nextTime.plusDays(1)
                     "WEEKLY" -> nextTime.plusWeeks(1)
                     "MONTHLY" -> nextTime.plusMonths(1)
                     "YEARLY" -> nextTime.plusYears(1)
                     else -> nextTime // Should not happen
                 }
            }
            timeInMillis = nextTime.toInstant().toEpochMilli()
        }

        if (timeInMillis > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                     alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact if permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelNotification(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleLoanReminder(context: Context, loan: com.shintopc.lifeorganiser.data.local.LoanEntry) {
        if (!loan.reminderEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Calculate Next Due Date Logic (Duplicate from VM for now, or preferably use a shared util, but standard copy is safer to avoid VM dependency here)
        val today = java.time.LocalDate.now()
        var nextDueDate = if (today.dayOfMonth <= loan.dueDay) {
             today.withDayOfMonth(loan.dueDay)
        } else {
             today.plusMonths(1).withDayOfMonth(loan.dueDay)
        }

        // Set to 9:00 AM
        val dueDateTime = java.time.LocalDateTime.of(nextDueDate, java.time.LocalTime.of(9, 0))
        val timeInMillis = dueDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // If time is in past (e.g. today 10 AM, due today 9 AM), schedule for next month
        val triggerTime = if (timeInMillis <= System.currentTimeMillis()) {
             dueDateTime.plusMonths(1).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
             timeInMillis
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Loan Payment Due")
            putExtra("message", "Payment of ₹${String.format("%.0f", loan.emiAmount)} is due for ${loan.title}")
            putExtra("id", loan.id.toLong())
            putExtra("isAlarm", loan.reminderType == "ALARM")
        }

        // Unique ID for Loan usually distinct from Reminder Entities?
        // ReminderEntity uses Long id. LoanEntry uses Int id. 
        // Collision risk if ReminderEntity ids overlap with LoanEntry ids.
        // Strategy: Use a large offset for loan request codes or rely on different 'extra' if needed?
        // PendingIntent request code collision implies cancelling each other.
        // Let's use negative IDs or a large offset (e.g. 100000 + loan.id) for loans to avoid collision with standard reminders.
        val requestCode = 1000000 + loan.id

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelLoanReminder(context: Context, loanId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val requestCode = 1000000 + loanId
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showLoanNotification(context: Context, title: String, message: String, notificationId: Int) {
        val intent = Intent(context, com.shintopc.lifeorganiser.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.shintopc.lifeorganiser.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
    fun scheduleTaskNotification(context: Context, task: com.shintopc.lifeorganiser.data.model.Task) {
        if (!task.isReminderOn || task.dueDate == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Task: ${task.title}")
            putExtra("message", task.description.ifEmpty { "You have a task due!" })
            // Using ID hashcode or safe string-to-long logic. 
            // Task IDs are UUID strings mostly. 
            // We need an INT request code. hashcode is best effort.
            putExtra("id", task.id.hashCode().toLong()) 
            putExtra("isAlarm", task.isAlarm) // Use isAlarm from task
        }

        val requestId = task.id.hashCode() // Use hashcode for pending intent ID

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Time Logic
        val dueDate = java.time.Instant.ofEpochMilli(task.dueDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val dueTime = if (task.dueTime != null) {
            java.time.Instant.ofEpochMilli(task.dueTime).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        } else {
            java.time.LocalTime.of(9, 0) // Default to 9 AM if no time set
        }
        
        val dueDateTime = java.time.LocalDateTime.of(dueDate, dueTime)
        var timeInMillis = dueDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (timeInMillis <= System.currentTimeMillis()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    fun scheduleDebtReminder(context: Context, debt: com.shintopc.lifeorganiser.data.local.FinanceDebt) {
        if (!debt.reminderEnabled || debt.reminderDate == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", if (debt.type == "LENT") "Payment due from ${debt.personName}" else "Payment due to ${debt.personName}")
            putExtra("message", "Amount: ₹${String.format("%.0f", debt.amount)}")
            putExtra("id", debt.id) // Long ID
            putExtra("isAlarm", debt.reminderType == "ALARM")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            debt.id.toInt() + 2000000, // Offset to avoid collision with loan/task
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Debt Reminder: Use exact time passed from UI (Combined Date + Time)
        val triggerTime = debt.reminderDate

        if (triggerTime <= System.currentTimeMillis()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
    
    fun cancelDebtReminder(context: Context, debtId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
             context,
            debtId.toInt() + 2000000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelTaskNotification(context: Context, task: com.shintopc.lifeorganiser.data.model.Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val requestId = task.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
