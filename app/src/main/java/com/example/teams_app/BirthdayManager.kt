package com.example.teams_app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*

class BirthdayManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs: SharedPreferences = context.getSharedPreferences("birthdays", Context.MODE_PRIVATE)
    private val channelId = "birthday_channel"

    private val memberMapping = mapOf(
        "Member1" to "Sakthi Sarvani R",
        "Member2" to "Sowndarya Meenakshi A",
        "Member3" to "Yogeetha K"
    )

    init {
        createNotificationChannel()
    }

    private fun getMemberFullName(memberKey: String): String {
        return memberMapping[memberKey] ?: memberKey
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Birthday Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Birthday reminder notifications"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun saveBirthday(birthday: Birthday) {
        prefs.edit().apply {
            putLong("${birthday.name}_date", birthday.date)
            putLong("${birthday.name}_time", birthday.time)
            putString("${birthday.name}_fullname", getMemberFullName(birthday.name))
            apply()
        }
        scheduleBirthdayNotification(birthday)
    }

    fun getBirthday(name: String): Birthday? {
        val date = prefs.getLong("${name}_date", -1)
        val time = prefs.getLong("${name}_time", -1)
        if (date == -1L || time == -1L) return null
        return Birthday(name, date, time)
    }

    fun reschedulePendingBirthdays() {
        val lastReschedule = prefs.getLong("last_reschedule", 0)
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastReschedule > 24 * 60 * 60 * 1000) {
            val birthdayNames = prefs.all.keys
                .filter { it.endsWith("_date") }
                .map { it.removeSuffix("_date") }
                .distinct()

            birthdayNames.forEach { name ->
                getBirthday(name)?.let { birthday ->
                    scheduleBirthdayNotification(birthday)
                }
            }

            prefs.edit().putLong("last_reschedule", currentTime).apply()
        }
    }

    private fun scheduleBirthdayNotification(birthday: Birthday) {
        val intent = Intent(context, BirthdayReceiver::class.java).apply {
            putExtra("name", birthday.name)
            putExtra("fullname", getMemberFullName(birthday.name))
            action = "com.example.teams_app.BIRTHDAY_NOTIFICATION_${birthday.name}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            birthday.name.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = birthday.date
            val timeCalendar = Calendar.getInstance().apply { timeInMillis = birthday.time }
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)

            if (timeInMillis < currentTime) {
                add(Calendar.YEAR, 1)
            }
        }

        alarmManager.cancel(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun showBirthdayNotification(name: String) {
        val fullName = prefs.getString("${name}_fullname", getMemberFullName(name)) ?: name

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
            .setContentTitle("Happy Birthday! $fullName 🎉")
            .setContentText("Wishing you a day filled with joy, laughter, and unforgettable moments!🎂🎈")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(name.hashCode(), notification)
    }
}