package com.example.teams_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BirthdayReceiver : BroadcastReceiver() {
    private val memberMapping = mapOf(
        "Member1" to "Sakthi Sarvani R",
        "Member2" to "Sowndarya Meenakshi A",
        "Member3" to "Yogeetha K"
    )

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BirthdayReceiver", "Received broadcast for birthday notification")
        val memberKey = intent.getStringExtra("name")

        if (memberKey != null) {
            val fullName = memberMapping[memberKey] ?: memberKey
            Log.d("BirthdayReceiver", "Showing notification for $fullName (key: $memberKey)")
            val birthdayManager = BirthdayManager(context)
            birthdayManager.showBirthdayNotification(memberKey)
        } else {
            Log.e("BirthdayReceiver", "Member key extra is null")
        }
    }
}