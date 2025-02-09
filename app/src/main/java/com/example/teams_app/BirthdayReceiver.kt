// BirthdayReceiver.kt
package com.example.teams_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BirthdayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BirthdayReceiver", "Received broadcast for birthday notification")
        val name = intent.getStringExtra("name")
        if (name != null) {
            Log.d("BirthdayReceiver", "Showing notification for $name")
            val birthdayManager = BirthdayManager(context)
            birthdayManager.showBirthdayNotification(name)
        } else {
            Log.e("BirthdayReceiver", "Name extra is null")
        }
    }
}