package com.example.teams_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.Toast
import android.util.Log

/**
 * Broadcast receiver with fixed notification logic for battery percentage thresholds.
 * This version ensures notifications are sent at each threshold level.
 */
class NetworkAndBatteryReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkAndBatteryReceiver"

        // Battery thresholds in descending order
        private val BATTERY_THRESHOLDS = listOf(100, 75, 50, 25, 15, 5)

        // Preference key to store last notified threshold
        private const val PREF_LAST_THRESHOLD = "last_battery_threshold"
        private const val PREF_AIRPLANE_MODE = "last_airplane_mode_state"
        private const val PREF_LAST_BATTERY_LEVEL = "last_battery_level"

        // Register this receiver with the application
        fun register(context: Context): NetworkAndBatteryReceiver {
            val receiver = NetworkAndBatteryReceiver()
            val filter = IntentFilter().apply {
                // For airplane mode changes
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                // For battery status updates
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }

            context.registerReceiver(receiver, filter)
            Log.d(TAG, "Receiver registered for airplane mode and battery monitoring")
            return receiver
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                handleAirplaneModeChange(context, intent)
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                handleBatteryLevelChange(context, intent)
            }
        }
    }

    private fun handleAirplaneModeChange(context: Context, intent: Intent) {
        val isAirplaneModeOn = intent.getBooleanExtra("state", false)

        // Get preferences to check the last airplane mode state
        val prefs = context.getSharedPreferences("BatteryPrefs", Context.MODE_PRIVATE)
        val lastAirplaneModeState = prefs.getBoolean(PREF_AIRPLANE_MODE, !isAirplaneModeOn)

        // Only notify if the state has changed
        if (isAirplaneModeOn != lastAirplaneModeState) {
            if (isAirplaneModeOn) {
                showAlert(context, "Airplane Mode Enabled",
                    "Airplane mode is now ON. Network connectivity is disabled.", 1001)
                logStatusChange(context, "Airplane Mode", "Enabled")
            } else {
                showAlert(context, "Airplane Mode Disabled",
                    "Airplane mode is now OFF. Network connectivity is restored.", 1002)
                logStatusChange(context, "Airplane Mode", "Disabled")
            }

            // Save the current airplane mode state
            prefs.edit().putBoolean(PREF_AIRPLANE_MODE, isAirplaneModeOn).apply()
        }
    }

    private fun handleBatteryLevelChange(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        if (level != -1 && scale != -1) {
            val batteryPct = level * 100 / scale

            // Get preferences
            val prefs = context.getSharedPreferences("BatteryPrefs", Context.MODE_PRIVATE)
            val lastBatteryLevel = prefs.getInt(PREF_LAST_BATTERY_LEVEL, -1)

            // Check if this is the first time we're checking battery level
            if (lastBatteryLevel == -1) {
                // First time, just save the current level
                prefs.edit().putInt(PREF_LAST_BATTERY_LEVEL, batteryPct).apply()

                // Find the current threshold
                val currentThreshold = BATTERY_THRESHOLDS.firstOrNull { it <= batteryPct } ?: 0

                // Check if we're exactly at a threshold
                if (BATTERY_THRESHOLDS.contains(batteryPct)) {
                    // We're exactly at a threshold, show notification
                    showBatteryNotification(context, batteryPct)
                    prefs.edit().putInt(PREF_LAST_THRESHOLD, batteryPct).apply()
                } else {
                    // Just update the threshold
                    prefs.edit().putInt(PREF_LAST_THRESHOLD, currentThreshold).apply()
                }

                return
            }

            // Only proceed if the battery level changed
            if (lastBatteryLevel != batteryPct) {
                Log.d(TAG, "Battery level changed: $lastBatteryLevel% -> $batteryPct%")

                // Save the current battery level
                prefs.edit().putInt(PREF_LAST_BATTERY_LEVEL, batteryPct).apply()

                // Get the last notified threshold
                val lastNotifiedThreshold = prefs.getInt(PREF_LAST_THRESHOLD, -1)

                // Check if we're exactly at a threshold value
                if (BATTERY_THRESHOLDS.contains(batteryPct)) {
                    // Direct threshold hit, show notification if we haven't already
                    if (batteryPct != lastNotifiedThreshold) {
                        showBatteryNotification(context, batteryPct)
                        prefs.edit().putInt(PREF_LAST_THRESHOLD, batteryPct).apply()
                        Log.d(TAG, "Battery exactly at threshold $batteryPct%, notification sent")
                    }
                } else {
                    // Find the current threshold range
                    val currentThreshold = BATTERY_THRESHOLDS.firstOrNull { it <= batteryPct } ?: 0

                    // Check if we crossed a threshold
                    var crossedThreshold: Int? = null

                    if (lastBatteryLevel > batteryPct) {
                        // Battery is discharging
                        for (threshold in BATTERY_THRESHOLDS) {
                            if (lastBatteryLevel > threshold && batteryPct <= threshold) {
                                crossedThreshold = threshold
                                break
                            }
                        }
                    } else {
                        // Battery is charging
                        for (threshold in BATTERY_THRESHOLDS.reversed()) {
                            if (lastBatteryLevel < threshold && batteryPct >= threshold) {
                                crossedThreshold = threshold
                                break
                            }
                        }
                    }

                    if (crossedThreshold != null && crossedThreshold != lastNotifiedThreshold) {
                        // We crossed a threshold, send notification
                        showBatteryNotification(context, crossedThreshold)
                        prefs.edit().putInt(PREF_LAST_THRESHOLD, crossedThreshold).apply()
                        Log.d(TAG, "Crossed threshold $crossedThreshold%, notification sent")
                    }

                    // Update the current threshold if needed
                    if (currentThreshold != lastNotifiedThreshold) {
                        prefs.edit().putInt(PREF_LAST_THRESHOLD, currentThreshold).apply()
                        Log.d(TAG, "Updated threshold tracker to $currentThreshold% without notification")
                    }
                }
            }
        }
    }

    private fun showBatteryNotification(context: Context, threshold: Int) {
        when (threshold) {
            5 -> {
                showAlert(context, "Critical Battery Level",
                    "Battery at $threshold%. Connect charger immediately! Device may shut down soon.", 2001)
                logStatusChange(context, "Battery Level", "Critical ($threshold%)")
            }
            15 -> {
                showAlert(context, "Very Low Battery",
                    "Battery at $threshold%. Please connect to a charger immediately!", 2002)
                logStatusChange(context, "Battery Level", "Very Low ($threshold%)")
            }
            25 -> {
                showAlert(context, "Low Battery",
                    "Battery at $threshold%. Consider connecting to a charger soon.", 2003)
                logStatusChange(context, "Battery Level", "Low ($threshold%)")
            }
            50 -> {
                showAlert(context, "Battery Half",
                    "Battery at $threshold%. Moderate usage recommended.", 2004)
                logStatusChange(context, "Battery Level", "Half ($threshold%)")
            }
            75 -> {
                showAlert(context, "Battery Good",
                    "Battery at $threshold%. Good battery level.", 2005)
                logStatusChange(context, "Battery Level", "Good ($threshold%)")
            }
            100 -> {
                showAlert(context, "Battery Full",
                    "Battery at $threshold%. Fully charged.", 2006)
                logStatusChange(context, "Battery Level", "Full ($threshold%)")
            }
        }
    }

    private fun showAlert(context: Context, title: String, message: String, notificationId: Int) {
        // Show a toast notification
        Toast.makeText(context, "$title: $message", Toast.LENGTH_LONG).show()

        // Send a system notification with unique ID
        NotificationHelper.showNotification(context, title, message, notificationId)
    }

    private fun logStatusChange(context: Context, eventType: String, status: String) {
        val timestamp = System.currentTimeMillis()
        val statusPrefs = context.getSharedPreferences("StatusLog", Context.MODE_PRIVATE)

        statusPrefs.edit().apply {
            putString("status_$timestamp", "$eventType|$status")
            commit() // Using commit() instead of apply() for immediate writing
        }

        Log.d(TAG, "Status logged: $eventType - $status")
    }
}