package com.example.teams_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import java.io.File

class MemberDetailActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentMindsetText: TextView
    private lateinit var mindsetConfirmationText: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var setMindsetButton: Button
    private lateinit var favoritesTabLayout: TabLayout
    private lateinit var favoritesContainer: LinearLayout
    private lateinit var addFavoriteButton: Button

    private lateinit var playlistManager: PlaylistManager
    private lateinit var playlistStatusText: TextView
    private lateinit var playlistControlButton: Button
    private lateinit var playlistSeekBar: SeekBar
    private lateinit var playlistProgressText: TextView

    private lateinit var dbHelper: FavoritesDbHelper
    private var currentTab = "songs"

    private val PREFS_NAME = "MindsetPrefs"
    private val MINDSET_PREFIX = "MemberMindset_"

    private val mindsetOptions = listOf("Happy", "Excited", "Calm", "Focused", "Tired", "Anxious", "Sad", "Lonely")
    private var currentMindset: String = "Happy"
    private var selectedMindset: String? = null
    private var memberName: String = ""
    private var selectedAudioPath: String? = null

    companion object {
        private const val AUDIO_PICK_REQUEST_CODE = 1001

        fun getMemberMindset(context: Context, memberName: String): String {
            val prefs = context.getSharedPreferences("MindsetPrefs", Context.MODE_PRIVATE)
            val sanitizedName = memberName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val key = "MemberMindset_" + sanitizedName
            return prefs.getString(key, "Happy") ?: "Happy"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_detail)

        dbHelper = FavoritesDbHelper(this)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize views
        currentMindsetText = findViewById(R.id.currentMindsetText)
        mindsetConfirmationText = findViewById(R.id.mindsetConfirmationText)
        chipGroup = findViewById(R.id.mindsetChipGroup)
        setMindsetButton = findViewById(R.id.setMindsetButton)
        favoritesTabLayout = findViewById(R.id.favoritesTabLayout)
        favoritesContainer = findViewById(R.id.favoritesContainer)
        addFavoriteButton = findViewById(R.id.addFavoriteButton)

        // Initialize playlist views
        playlistStatusText = findViewById(R.id.playlistStatusText)
        playlistControlButton = findViewById(R.id.playlistControlButton)
        playlistSeekBar = findViewById(R.id.playlistSeekBar)
        playlistProgressText = findViewById(R.id.playlistProgressText)

        // Extract member details from intent
        memberName = intent.getStringExtra("MEMBER_NAME") ?: ""
        val role = intent.getStringExtra("MEMBER_ROLE") ?: ""
        val email = intent.getStringExtra("MEMBER_EMAIL") ?: ""
        val phone = intent.getStringExtra("MEMBER_PHONE") ?: ""
        val description = intent.getStringExtra("MEMBER_DESCRIPTION") ?: ""
        val imageResId = intent.getIntExtra("MEMBER_IMAGE", 0)

        // Set member details in views
        findViewById<TextView>(R.id.toolbarTitle).text = memberName
        findViewById<TextView>(R.id.memberName).text = memberName
        findViewById<TextView>(R.id.memberRole).text = role
        findViewById<TextView>(R.id.memberDescription).text = description
        findViewById<TextView>(R.id.memberEmail).text = email
        findViewById<TextView>(R.id.memberPhone).text = phone
        findViewById<ImageView>(R.id.memberImage).setImageResource(imageResId)

        // Load and setup mindset
        loadCurrentMindset()
        setupMindsetChips()

        // Mindset set button listener
        setMindsetButton.setOnClickListener {
            if (selectedMindset != null) {
                saveMindset(selectedMindset!!)
                showConfirmationMessage("${memberName}'s mindset changed to ${selectedMindset}!")
            } else {
                showConfirmationMessage("Please select a mindset first", isError = true)
            }
        }

        // Setup favorites tab layout
        setupFavoritesTabLayout()

        // Add favorite button listener
        addFavoriteButton.setOnClickListener {
            showAddFavoriteDialog()
        }

        // Setup playlist manager
        playlistManager = PlaylistManager(this, memberName)
        playlistManager.setupPlaylistControls(
            playlistStatusText,
            playlistControlButton,
            playlistSeekBar,
            playlistProgressText
        )

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
        favoritesContainer.removeAllViews()

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
        val uploadButton = dialogView.findViewById<Button>(R.id.uploadAudioButton)
        val extraLabel = dialogView.findViewById<TextView>(R.id.extraLabel)

        // Reset selected audio path
        selectedAudioPath = null

        val itemType = if (currentTab == "songs") "Song" else "Movie"
        val extraType = if (currentTab == "songs") "Genre" else "Language"
        extraLabel.text = extraType

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(
                Intent.createChooser(intent, "Select Audio File"),
                AUDIO_PICK_REQUEST_CODE
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Favorite $itemType")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val extra = extraEditText.text.toString().trim()

                if (name.isNotEmpty()) {
                    val id = dbHelper.addFavorite(
                        memberName,
                        currentTab,
                        name,
                        extra,
                        selectedAudioPath
                    )

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUDIO_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Save the audio file to app's internal storage
                selectedAudioPath = saveAudioFile(uri)
                Toast.makeText(this, "Audio file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAudioFile(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = getFileName(uri)
        val audioFile = File(filesDir, fileName)

        inputStream?.use { input ->
            audioFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return audioFile.absolutePath
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "audio_${System.currentTimeMillis()}.mp3"
    }

    private fun deleteFavorite(id: Int) {
        val success = dbHelper.deleteFavorite(id)
        if (success) {
            loadFavorites(currentTab)
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMindsetKeyForMember(memberName: String): String {
        val sanitizedName = memberName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return MINDSET_PREFIX + sanitizedName
    }

    private fun loadCurrentMindset() {
        val mindsetKey = getMindsetKeyForMember(memberName)
        currentMindset = sharedPreferences.getString(mindsetKey, "Happy") ?: "Happy"
        currentMindsetText.text = currentMindset
    }

    private fun setupMindsetChips() {
        chipGroup.removeAllViews()

        mindsetOptions.forEach { mindset ->
            val chip = Chip(this)
            chip.text = mindset
            chip.isCheckable = true
            chip.isChecked = mindset == currentMindset

            chip.setOnClickListener {
                selectedMindset = mindset
                updateSelectedChip(chipGroup, chip)
            }

            chipGroup.addView(chip)
        }
    }

    private fun updateSelectedChip(chipGroup: ChipGroup, selectedChip: Chip) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            chip?.isChecked = chip == selectedChip
        }
    }

    private fun saveMindset(mindset: String) {
        val mindsetKey = getMindsetKeyForMember(memberName)
        currentMindset = mindset
        sharedPreferences.edit().putString(mindsetKey, mindset).apply()
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

        mindsetConfirmationText.visibility = View.VISIBLE

        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 500

        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 500
        fadeOut.startOffset = 2000

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

    override fun onDestroy() {
        super.onDestroy()
        playlistManager.release()
    }
}