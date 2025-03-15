package com.example.teams_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val timeTableManager = TimeTableNotificationManager(context)

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            timeTableManager.scheduleNotifications()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                timeTableManager.checkAndNotify()
            }
        }
    }
}
