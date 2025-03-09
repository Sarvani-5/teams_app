package com.example.teams_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MemberDetailActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "MindsetPrefs"
    private val MINDSET_PREFIX = "MemberMindset_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_detail)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Set toolbar title
        findViewById<TextView>(R.id.toolbarTitle).text = intent.getStringExtra("MEMBER_NAME") ?: "Team Member"

        // Get member ID from intent
        val memberId = intent.getStringExtra("MEMBER_ID") ?: ""

        if (memberId.isNotEmpty()) {
            // Create and show the fragment
            if (savedInstanceState == null) {
                val fragment = TeamMemberDetailFragment.newInstance(memberId)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
            }
        } else {
            // Handle error when no member ID is provided
            // You could show a toast message or finish the activity
        }
    }

    // Method to get a member's mindset (can be used from other parts of the app)
    companion object {
        fun getMemberMindset(context: Context, memberName: String): String {
            val prefs = context.getSharedPreferences("MindsetPrefs", Context.MODE_PRIVATE)
            val sanitizedName = memberName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val key = "MemberMindset_" + sanitizedName
            return prefs.getString(key, "Happy") ?: "Happy"
        }

        // Launch this activity with the proper parameters
        fun launch(context: Context, memberId: String, memberName: String) {
            val intent = Intent(context, MemberDetailActivity::class.java).apply {
                putExtra("MEMBER_ID", memberId)
                putExtra("MEMBER_NAME", memberName)
            }
            context.startActivity(intent)
        }
    }
}