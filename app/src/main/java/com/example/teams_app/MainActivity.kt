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

        // Schedule notifications
        timeTableNotificationManager.scheduleNotifications()

        // Request permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestRequiredPermissions()
        }

        // Show welcome toast
        Toast.makeText(this, "Welcome! Class and birthday notifications are active", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check notification permission
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check alarm permission
        if (checkSelfPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM) !=
            PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
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
                var allGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                        break
                    }
                }

                if (allGranted) {
                    Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
                    // Reschedule notifications since permissions are now granted
                    birthdayManager.reschedulePendingBirthdays()
                } else {
                    Toast.makeText(this,
                        "Please enable all permissions in settings for full functionality",
                        Toast.LENGTH_LONG).show()
                }
            }
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
            else -> return super.onOptionsItemSelected(item)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        return true
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