package com.example.teams_app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var logoView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Tamil Roots"
        supportActionBar?.subtitle = "Innovating for Tomorrow"

        // Initialize logo view
        logoView = findViewById(R.id.logoImage)

        // Show only logo on startup
        if (savedInstanceState == null) {
            logoView.setVisibility(View.VISIBLE)  // Show logo on main page
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.menu_about -> {
                logoView.setVisibility(View.GONE)  // Hide logo when navigating to About Us
                AboutUsFragment()
            }
            R.id.menu_team_details -> {
                logoView.setVisibility(View.GONE)  // Hide logo when navigating to other fragments
                TeamDetailsFragment()
            }
            R.id.menu_team_members -> {
                logoView.setVisibility(View.GONE)
                TeamMembersFragment()
            }
            R.id.menu_project -> {
                logoView.setVisibility(View.GONE)
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
}
