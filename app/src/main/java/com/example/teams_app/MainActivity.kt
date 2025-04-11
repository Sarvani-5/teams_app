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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private lateinit var logoView: ImageView
    private lateinit var timeTableNotificationManager: TimeTableNotificationManager
    private lateinit var birthdayManager: BirthdayManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Add reference to the ReceiverManager
    private val receiverManager = ReceiverManager

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        // Add SMS permission for the team members alerts
        Manifest.permission.SEND_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Tamil Roots"
            subtitle = "Innovating for Tomorrow"
        }

        // Initialize views and managers
        logoView = findViewById(R.id.logoImage)
        timeTableNotificationManager = TimeTableNotificationManager(this)
        birthdayManager = BirthdayManager(this)

        if (savedInstanceState == null) {
            logoView.visibility = View.VISIBLE
        }

        // Create notification channel for the system alerts
        NotificationHelper.createNotificationChannel(this)

        // Check permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestRequiredPermissions()
        } else {
            checkAndScheduleNotifications()
        }

        // Register broadcast receivers for network and battery monitoring
        receiverManager.registerReceivers(applicationContext)
    }

    private fun checkAndScheduleNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                scheduleNotifications()
            }
        } else {
            scheduleNotifications()
        }
    }

    private fun scheduleNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                timeTableNotificationManager.scheduleNotifications()
                birthdayManager.reschedulePendingBirthdays()
                showWelcomeMessage()
            } else {
                showExactAlarmPermissionDialog()
            }
        } else {
            timeTableNotificationManager.scheduleNotifications()
            birthdayManager.reschedulePendingBirthdays()
            showWelcomeMessage()
        }
    }

    private fun showWelcomeMessage() {
        Toast.makeText(
            this,
            "Welcome! Class, system, and birthday notifications are active",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs permission to schedule exact alarms for class notifications. Please enable this in Settings.")
            .setPositiveButton("Settings") { _, _ ->
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

        for (permission in requiredPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            checkAndScheduleNotifications()
        }
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
            R.id.menu_location_tracking -> {
                logoView.visibility = View.GONE
                LocationTrackingFragment()
            }
            R.id.menu_system_status -> {
                logoView.visibility = View.GONE
                SystemStatusFragment()
            }
            // Add the new menu option handler for custom graphics
            R.id.menu_custom_graphics -> {
                logoView.visibility = View.GONE
                CustomGraphicsFragment()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        return true
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
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                ) {
                    checkAndScheduleNotifications()
                } else {
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

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
            ) {
                checkAndScheduleNotifications()
            }
        }

        // Re-register receivers in case they were unregistered
        receiverManager.registerReceivers(applicationContext)
    }

    override fun onBackPressed() {
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
        // Unregister the broadcast receivers to prevent memory leaks
        receiverManager.unregisterReceivers(applicationContext)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }
}