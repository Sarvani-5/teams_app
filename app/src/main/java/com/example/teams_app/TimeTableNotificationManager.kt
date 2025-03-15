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
import android.graphics.Color

class TimeTableNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val channelId = "class_schedule_channel"

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
                setShowBadge(true)
                lockscreenVisibility = NotificationManager.IMPORTANCE_HIGH
                enableLights(true)
                lightColor = Color.BLUE
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotifications() {
        // Schedule next check in 1 minute
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAndNotify() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentTime = LocalTime.now()

        val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val today = days[dayOfWeek - 1]

        if (today == "Sunday" || today == "Saturday") {
            scheduleNotifications() // Schedule next check even on weekends
            return
        }

        TimeTableData.timeTable[today]?.forEach { (time, subject) ->
            val scheduleTime = parseTime(time)

            if (currentTime.hour == scheduleTime.hour &&
                currentTime.minute == scheduleTime.minute) {

                val baseSubject = subject.split("[")[0].trim()
                val fullSubjectName = TimeTableData.subjectFullNames[baseSubject] ?: subject

                // Customize notification message based on type
                val message = when (baseSubject) {
                    "Break" -> "It's time for a break!"
                    "Lunch" -> "It's lunch time!"
                    else -> "Your $fullSubjectName class is starting now"
                }

                sendNotification(
                    when (baseSubject) {
                        "Break", "Lunch" -> "Time Reminder"
                        else -> "Class Reminder"
                    },
                    message,
                    subject.hashCode()
                )
            }
        }

        // Schedule next check
        scheduleNotifications()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(timeString: String): LocalTime {
        val standardizedTime = timeString.replace(".", ":")
        return LocalTime.parse(standardizedTime, DateTimeFormatter.ofPattern("H:mm"))
    }

    private fun sendNotification(title: String, content: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .build()

        notificationManager.notify(notificationId, notification)
    }
}