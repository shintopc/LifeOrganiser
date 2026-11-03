package com.shintopc.lifeorganiser.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.shintopc.lifeorganiser.MainActivity
import com.shintopc.lifeorganiser.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "Time for your task!"
        val id = intent.getLongExtra("id", 0)
        val isAlarm = intent.getBooleanExtra("isAlarm", false)
        
        // Intent to open app when notification is clicked
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(), // PendingIntent requires an Int request code
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isAlarm) NotificationHelper.ALARM_CHANNEL_ID else NotificationHelper.CHANNEL_ID

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Open app on click

        if (isAlarm) {
            notificationBuilder
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM))
        }

        val notification = notificationBuilder.build()
        if (isAlarm) {
            notification.flags = notification.flags or android.app.Notification.FLAG_INSISTENT
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(if (id == 0L) System.currentTimeMillis().toInt() else id.toInt(), notification)
    }
}
