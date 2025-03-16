package com.example.teams_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Helper class to manage notifications for battery level and airplane mode changes.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "battery_airplane_channel"
    private const val CHANNEL_NAME = "Battery and Airplane Mode Updates"
    private const val CHANNEL_DESCRIPTION = "Notifications for battery level and airplane mode changes"

    /**
     * Creates the notification channel for Android O and above.
     * This method is public so it can be called from MainActivity.
     */
    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ (Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a notification with the given title and message.
     * Each notification has a unique ID so they appear as separate notifications.
     */
    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        // Create the notification channel for Android O and above
        createNotificationChannel(context)

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show the notification with unique ID
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle case where notification permission is not granted
                android.util.Log.e("NotificationHelper", "No permission to show notification", e)
            }
        }
    }
}