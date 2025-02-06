package com.example.teams_app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var logoView: ImageView
    private lateinit var notificationScheduler: NotificationScheduler

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

        // Initialize and schedule notifications
        try {
            notificationScheduler = NotificationScheduler(this)
            notificationScheduler.scheduleTimetableNotifications()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to schedule notifications: ${e.message}", Toast.LENGTH_LONG).show()
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
            // If there are fragments in the back stack, show the logo
            logoView.visibility = View.VISIBLE
            supportFragmentManager.popBackStack()
        } else {
            // If we're at the root, show exit dialog
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
        // Clean up any resources if needed
    }
}