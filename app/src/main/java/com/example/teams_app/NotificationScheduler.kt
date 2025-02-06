package com.example.teams_app

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

data class TimetableEntry(
    val dayOfWeek: Int,
    val hour: Int,
    val minute: Int,
    val subject: String
)

class NotificationScheduler(private val context: Context) {
    private val timetable = listOf(
        // Monday
        TimetableEntry(Calendar.MONDAY, 9, 0, "22IT680"),
        TimetableEntry(Calendar.MONDAY, 9, 50, "22IT680"),
        TimetableEntry(Calendar.MONDAY, 10, 40, "22ITPCD/PFD"),
        TimetableEntry(Calendar.MONDAY, 11, 30, "22ITPE/D/PGD"),
        TimetableEntry(Calendar.MONDAY, 2, 30, "22EG660 (T)"),
        TimetableEntry(Calendar.MONDAY, 3, 20, "22IT620"),
        TimetableEntry(Calendar.MONDAY, 4, 10, "22IT690"),

        // Add other days similarly
        TimetableEntry(Calendar.THURSDAY, 9, 0, "22ITPED/PGD"),
        TimetableEntry(Calendar.THURSDAY, 9, 50, "22IT610"),
        TimetableEntry(Calendar.THURSDAY, 10, 40, "22EG660 (P)"),
        TimetableEntry(Calendar.THURSDAY, 11, 30, "22EG660 (P)"),
        TimetableEntry(Calendar.THURSDAY, 2, 30, "22IT620"),
        TimetableEntry(Calendar.THURSDAY, 3, 20, "22IT650 (T)"),
        TimetableEntry(Calendar.THURSDAY, 4, 10, "22IT690")
        // Add other days as needed
    )

    fun scheduleTimetableNotifications() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (entry in timetable) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("subject", entry.subject)
            }

            // Create unique ID for each class
            val requestCode = (entry.dayOfWeek * 10000) + (entry.hour * 100) + entry.minute

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, entry.dayOfWeek)
                set(Calendar.HOUR_OF_DAY, entry.hour)
                set(Calendar.MINUTE, entry.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If time has passed today, schedule for next week
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            // Schedule weekly repeating alarm
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7, // Weekly repeat
                pendingIntent
            )
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subject = intent.getStringExtra("subject") ?: "Upcoming Class"
        showNotification(context, "Class Status", "Current class: $subject")
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "timetable_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Timetable Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(title.hashCode(), notification)
    }
}