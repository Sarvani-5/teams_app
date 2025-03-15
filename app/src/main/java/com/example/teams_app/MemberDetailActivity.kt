package com.example.teams_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout

class MemberDetailActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentMindsetText: TextView
    private lateinit var mindsetConfirmationText: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var setMindsetButton: Button
    private lateinit var favoritesTabLayout: TabLayout
    private lateinit var favoritesContainer: LinearLayout
    private lateinit var addFavoriteButton: Button

    private lateinit var dbHelper: FavoritesDbHelper
    private var currentTab = "songs" // Default tab

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

        // Initialize SQLite database helper
        dbHelper = FavoritesDbHelper(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize views
        currentMindsetText = findViewById(R.id.currentMindsetText)
        mindsetConfirmationText = findViewById(R.id.mindsetConfirmationText)
        chipGroup = findViewById(R.id.mindsetChipGroup)
        setMindsetButton = findViewById(R.id.setMindsetButton)
        favoritesTabLayout = findViewById(R.id.favoritesTabLayout)
        favoritesContainer = findViewById(R.id.favoritesContainer)
        addFavoriteButton = findViewById(R.id.addFavoriteButton)

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

        // Set up favorites tab layout
        setupFavoritesTabLayout()

        // Set up add favorite button
        addFavoriteButton.setOnClickListener {
            showAddFavoriteDialog()
        }

        // Load initial favorites
        loadFavorites(currentTab)
    }

    private fun setupFavoritesTabLayout() {
        favoritesTabLayout.addTab(favoritesTabLayout.newTab().setText("Songs"))
        favoritesTabLayout.addTab(favoritesTabLayout.newTab().setText("Movies"))

        favoritesTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = if (tab.position == 0) "songs" else "movies"
                loadFavorites(currentTab)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadFavorites(type: String) {
        // Clear existing items
        favoritesContainer.removeAllViews()

        // Get favorites from database
        val favorites = dbHelper.getFavorites(memberName, type)

        if (favorites.isEmpty()) {
            val noFavoritesText = TextView(this)
            noFavoritesText.text = "No favorite ${type} added yet"
            noFavoritesText.textSize = 16f
            noFavoritesText.setPadding(16, 16, 16, 16)
            favoritesContainer.addView(noFavoritesText)
        } else {
            for (favorite in favorites) {
                val favoriteView = LayoutInflater.from(this).inflate(R.layout.item_favourite, null)
                val nameText = favoriteView.findViewById<TextView>(R.id.favoriteName)
                val extraText = favoriteView.findViewById<TextView>(R.id.favoriteExtra)
                val deleteButton = favoriteView.findViewById<ImageView>(R.id.deleteButton)

                nameText.text = favorite.name
                extraText.text = favorite.extra
                deleteButton.setOnClickListener {
                    deleteFavorite(favorite.id)
                }

                favoritesContainer.addView(favoriteView)
            }
        }
    }

    private fun showAddFavoriteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_favorite, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.favoriteNameEditText)
        val extraEditText = dialogView.findViewById<EditText>(R.id.favoriteExtraEditText)
        val extraLabel = dialogView.findViewById<TextView>(R.id.extraLabel)

        // Set appropriate labels based on current tab
        val itemType = if (currentTab == "songs") "Song" else "Movie"
        val extraType = if (currentTab == "songs") "Genre" else "Language"
        extraLabel.text = extraType

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Favorite $itemType")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val extra = extraEditText.text.toString().trim()

                if (name.isNotEmpty()) {
                    val id = dbHelper.addFavorite(memberName, currentTab, name, extra)
                    if (id > 0) {
                        loadFavorites(currentTab)
                        Toast.makeText(this, "Favorite $itemType added", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun deleteFavorite(id: Int) {
        val success = dbHelper.deleteFavorite(id)
        if (success) {
            loadFavorites(currentTab)
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
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