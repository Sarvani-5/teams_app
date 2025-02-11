package com.example.teams_app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var logoView: ImageView
    private lateinit var timeTableNotificationManager: TimeTableNotificationManager
    private lateinit var birthdayManager: BirthdayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Tamil Roots"
            subtitle = "Innovating for Tomorrow"
        }

        // Initialize logo
        logoView = findViewById(R.id.logoImage)
        if (savedInstanceState == null) {
            logoView.visibility = View.VISIBLE
        }

        // Initialize managers
        timeTableNotificationManager = TimeTableNotificationManager(this)
        birthdayManager = BirthdayManager(this)

        // Check and request permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestRequiredPermissions()
        } else {
            checkAndScheduleNotifications()
        }
    }

    private fun checkAndScheduleNotifications() {
        // Check if we have notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                scheduleNotifications()
            }
        } else {
            // For lower Android versions, just schedule
            scheduleNotifications()
        }
    }

    private fun scheduleNotifications() {
        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                timeTableNotificationManager.scheduleNotifications()
                birthdayManager.reschedulePendingBirthdays()
                showWelcomeMessage()
            } else {
                // Show dialog to request exact alarm permission
                showExactAlarmPermissionDialog()
            }
        } else {
            // For lower Android versions, just schedule
            timeTableNotificationManager.scheduleNotifications()
            birthdayManager.reschedulePendingBirthdays()
            showWelcomeMessage()
        }
    }

    private fun showWelcomeMessage() {
        Toast.makeText(
            this,
            "Welcome! Class and birthday notifications are active",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs permission to schedule exact alarms for class notifications. Please enable this in Settings.")
            .setPositiveButton("Settings") { _, _ ->
                // Open exact alarm settings
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showWelcomeMessage()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check notification permission
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Show dialog for exact alarm permission
                showExactAlarmPermissionDialog()
            }
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // All permissions are granted, proceed with scheduling
            checkAndScheduleNotifications()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All requested permissions granted
                    checkAndScheduleNotifications()
                } else {
                    // Show settings dialog if permissions denied
                    showPermissionSettingsDialog()
                }
            }
        }
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Please enable required permissions in Settings for the app to function properly.")
            .setPositiveButton("Settings") { _, _ ->
                // Open app settings
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = android.net.Uri.fromParts("package", packageName, null)
                })
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showWelcomeMessage()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.menu_about -> {
                logoView.visibility = View.GONE
                AboutUsFragment()
            }
            R.id.menu_team_details -> {
                logoView.visibility = View.GONE
                TeamDetailsFragment()
            }
            R.id.menu_team_members -> {
                logoView.visibility = View.GONE
                TeamMembersFragment()
            }
            R.id.menu_project -> {
                logoView.visibility = View.GONE
                ProjectDescriptionFragment()
            }
            R.id.menu_birthdays -> {
                logoView.visibility = View.GONE
                BirthdayFragment()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        return true
    }

    override fun onResume() {
        super.onResume()
        // Check permissions again in case they were changed in Settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
            ) {
                checkAndScheduleNotifications()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount > 0) {
            logoView.visibility = View.VISIBLE
            supportFragmentManager.popBackStack()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Notifications will continue even after app closure
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }
}