package com.example.teams_app

import android.content.Context
import android.util.Log

/**
 * Manager class to handle registration and unregistration of broadcast receivers
 */
object ReceiverManager {
    private const val TAG = "ReceiverManager"
    private var networkAndBatteryReceiver: NetworkAndBatteryReceiver? = null

    /**
     * Registers all the application's broadcast receivers
     */
    fun registerReceivers(context: Context) {
        try {
            // Create notification channel for Android 8.0+
            NotificationHelper.createNotificationChannel(context)

            // Register network and battery receiver
            if (networkAndBatteryReceiver == null) {
                networkAndBatteryReceiver = NetworkAndBatteryReceiver.register(context)
                Log.d(TAG, "Network and battery receiver registered successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering receivers: ${e.message}")
        }
    }

    /**
     * Unregisters all the application's broadcast receivers
     */
    fun unregisterReceivers(context: Context) {
        try {
            // Unregister network and battery receiver
            networkAndBatteryReceiver?.let {
                context.unregisterReceiver(it)
                networkAndBatteryReceiver = null
                Log.d(TAG, "Network and battery receiver unregistered successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers: ${e.message}")
        }
    }
}