package com.example.teams_app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimeTableNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val channelId = "class_schedule_channel"
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("H:mm")

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Class Schedule",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for class schedule"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotifications() {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            60000,
            pendingIntent
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAndNotify() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentTime = LocalTime.now()

        val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val today = days[dayOfWeek - 1]

        if (today == "Sunday" || today == "Saturday") return

        TimeTableData.timeTable[today]?.forEach { (time, subject) ->
            val scheduleTime = LocalTime.parse(time, formatter)

            if (currentTime.hour == scheduleTime.hour &&
                currentTime.minute >= scheduleTime.minute &&
                currentTime.minute < scheduleTime.minute + 1) {

                val fullSubjectName = TimeTableData.subjectFullNames[subject.split("[")[0]] ?: subject
                sendNotification(
                    "Class Reminder",
                    "Your $fullSubjectName class is starting now",
                    subject.hashCode()
                )
            }
        }
    }

    private fun sendNotification(title: String, content: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .build()

        notificationManager.notify(notificationId, notification)
    }
}