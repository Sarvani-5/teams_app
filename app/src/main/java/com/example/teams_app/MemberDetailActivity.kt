package com.example.teams_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MemberDetailActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentMindsetText: TextView
    private lateinit var mindsetConfirmationText: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var setMindsetButton: Button

    private val PREFS_NAME = "MindsetPrefs"
    private val MINDSET_PREFIX = "MemberMindset_"

    // Define available mindset options
    private val mindsetOptions = listOf("Happy", "Excited", "Calm", "Focused", "Tired", "Anxious", "Sad", "Lonely")
    private var currentMindset: String = "Happy" // Default value
    private var selectedMindset: String? = null
    private var memberName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_detail)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize views
        currentMindsetText = findViewById(R.id.currentMindsetText)
        mindsetConfirmationText = findViewById(R.id.mindsetConfirmationText)
        chipGroup = findViewById(R.id.mindsetChipGroup)
        setMindsetButton = findViewById(R.id.setMindsetButton)

        // Get data from intent
        memberName = intent.getStringExtra("MEMBER_NAME") ?: ""
        val role = intent.getStringExtra("MEMBER_ROLE") ?: ""
        val email = intent.getStringExtra("MEMBER_EMAIL") ?: ""
        val phone = intent.getStringExtra("MEMBER_PHONE") ?: ""
        val description = intent.getStringExtra("MEMBER_DESCRIPTION") ?: ""
        val imageResId = intent.getIntExtra("MEMBER_IMAGE", 0)

        // Set up views
        findViewById<TextView>(R.id.toolbarTitle).text = memberName
        findViewById<TextView>(R.id.memberName).text = memberName
        findViewById<TextView>(R.id.memberRole).text = role
        findViewById<TextView>(R.id.memberDescription).text = description
        findViewById<TextView>(R.id.memberEmail).text = email
        findViewById<TextView>(R.id.memberPhone).text = phone
        findViewById<ImageView>(R.id.memberImage).setImageResource(imageResId)

        // Load and display the current mindset for this specific member
        loadCurrentMindset()

        // Set up mindset selection chips
        setupMindsetChips()

        // Set up the "Set Mindset" button
        setMindsetButton.setOnClickListener {
            if (selectedMindset != null) {
                saveMindset(selectedMindset!!)
                showConfirmationMessage("${memberName}'s mindset changed to ${selectedMindset}!")
            } else {
                showConfirmationMessage("Please select a mindset first", isError = true)
            }
        }
    }

    private fun getMindsetKeyForMember(memberName: String): String {
        // Create a unique key for each member by combining the prefix and the member's name
        // We sanitize the name to make it safe for use as a preference key
        val sanitizedName = memberName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return MINDSET_PREFIX + sanitizedName
    }

    private fun loadCurrentMindset() {
        // Get the mindset key for this specific member
        val mindsetKey = getMindsetKeyForMember(memberName)

        // Retrieve the last saved mindset for this member from SharedPreferences
        currentMindset = sharedPreferences.getString(mindsetKey, "Happy") ?: "Happy"
        currentMindsetText.text = currentMindset
    }

    private fun setupMindsetChips() {
        // Clear any existing chips
        chipGroup.removeAllViews()

        // Create chips for each mindset option
        mindsetOptions.forEach { mindset ->
            val chip = Chip(this)
            chip.text = mindset
            chip.isCheckable = true
            chip.isChecked = mindset == currentMindset // Check the current mindset

            // Set click listener for the chip
            chip.setOnClickListener {
                selectedMindset = mindset
                updateSelectedChip(chipGroup, chip)
            }

            chipGroup.addView(chip)
        }
    }

    private fun updateSelectedChip(chipGroup: ChipGroup, selectedChip: Chip) {
        // Uncheck all chips
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            chip?.isChecked = chip == selectedChip
        }
    }

    private fun saveMindset(mindset: String) {
        // Get the mindset key for this specific member
        val mindsetKey = getMindsetKeyForMember(memberName)

        // Save the selected mindset to SharedPreferences for this specific member
        currentMindset = mindset
        sharedPreferences.edit().putString(mindsetKey, mindset).apply()

        // Update the display
        currentMindsetText.text = mindset
    }

    private fun showConfirmationMessage(message: String, isError: Boolean = false) {
        mindsetConfirmationText.text = message
        mindsetConfirmationText.setTextColor(
            resources.getColor(
                if (isError) android.R.color.holo_red_dark else R.color.teal_700,
                null
            )
        )

        // Make sure the text is visible
        mindsetConfirmationText.visibility = View.VISIBLE

        // Add a fade animation
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 500

        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 500
        fadeOut.startOffset = 2000 // Start fading out after 2 seconds

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mindsetConfirmationText.startAnimation(fadeOut)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mindsetConfirmationText.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        mindsetConfirmationText.startAnimation(fadeIn)
    }

    // Method to get a member's mindset (can be used from other parts of the app)
    companion object {
        fun getMemberMindset(context: Context, memberName: String): String {
            val prefs = context.getSharedPreferences("MindsetPrefs", Context.MODE_PRIVATE)
            val sanitizedName = memberName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val key = "MemberMindset_" + sanitizedName
            return prefs.getString(key, "Happy") ?: "Happy"
        }
    }
}