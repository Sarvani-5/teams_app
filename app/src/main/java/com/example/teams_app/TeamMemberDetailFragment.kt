package com.example.teams_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore

class TeamMemberDetailFragment : Fragment() {
    private lateinit var imageViewMember: ImageView
    private lateinit var textViewName: TextView
    private lateinit var textViewRole: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewPhone: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var progressBar: ProgressBar

    // Mindset UI components
    private lateinit var currentMindsetText: TextView
    private lateinit var mindsetConfirmationText: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var setMindsetButton: Button
    private lateinit var favoriteSongsButton: Button
    private lateinit var favoriteMoviesButton: Button

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "MindsetPrefs"
    private val MINDSET_PREFIX = "MemberMindset_"

    // Define available mindset options
    private val mindsetOptions = listOf("Happy", "Excited", "Calm", "Focused", "Tired", "Anxious", "Sad", "Lonely")
    private var currentMindset: String = "Happy" // Default value
    private var selectedMindset: String? = null
    private var memberName: String = ""

    private var memberId: String? = null

    companion object {
        private const val TAG = "MemberDetailFragment"
        private const val ARG_MEMBER_ID = "member_id"

        fun newInstance(memberId: String): TeamMemberDetailFragment {
            val fragment = TeamMemberDetailFragment()
            val args = Bundle()
            args.putString(ARG_MEMBER_ID, memberId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            memberId = it.getString(ARG_MEMBER_ID)
        }

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_member_detail, container, false)

        // Initialize views
        imageViewMember = view.findViewById(R.id.imageViewMemberDetail)
        textViewName = view.findViewById(R.id.textViewNameDetail)
        textViewRole = view.findViewById(R.id.textViewRoleDetail)
        textViewEmail = view.findViewById(R.id.textViewEmail)
        textViewPhone = view.findViewById(R.id.textViewPhone)
        textViewDescription = view.findViewById(R.id.textViewDescription)
        progressBar = view.findViewById(R.id.progressBarDetail)

        // Initialize mindset views
        currentMindsetText = view.findViewById(R.id.currentMindsetText)
        mindsetConfirmationText = view.findViewById(R.id.mindsetConfirmationText)
        chipGroup = view.findViewById(R.id.mindsetChipGroup)
        setMindsetButton = view.findViewById(R.id.setMindsetButton)
        favoriteSongsButton = view.findViewById(R.id.favoriteSongsButton)
        favoriteMoviesButton = view.findViewById(R.id.favoriteMoviesButton)

        memberId?.let { id ->
            fetchMemberDetails(id)
        } ?: run {
            Toast.makeText(context, "Member ID not provided", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the "Set Mindset" button
        setMindsetButton.setOnClickListener {
            if (selectedMindset != null) {
                saveMindset(selectedMindset!!)
                showConfirmationMessage("${memberName}'s mindset changed to ${selectedMindset}!")
            } else {
                showConfirmationMessage("Please select a mindset first", isError = true)
            }
        }

        // Set up favorite songs button
        favoriteSongsButton.setOnClickListener {
            val intent = Intent(requireContext(), FavoritesActivity::class.java).apply {
                putExtra("MEMBER_NAME", memberName)
                putExtra("ITEM_TYPE", "song")
            }
            startActivity(intent)
        }

        // Set up favorite movies button
        favoriteMoviesButton.setOnClickListener {
            val intent = Intent(requireContext(), FavoritesActivity::class.java).apply {
                putExtra("MEMBER_NAME", memberName)
                putExtra("ITEM_TYPE", "movie")
            }
            startActivity(intent)
        }
    }

    private fun fetchMemberDetails(memberId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Show loading state
        progressBar.visibility = View.VISIBLE

        firestore.collection("team_members").document(memberId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val member = MemberInfo(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            role = document.getString("role") ?: "",
                            email = document.getString("email") ?: "",
                            phone = document.getString("phone") ?: "",
                            description = document.getString("description") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            imageResId = document.getLong("imageResId")?.toInt() ?: 0
                        )

                        memberName = member.name
                        displayMemberDetails(member)

                        // Load mindset after we have the member name
                        loadCurrentMindset()
                        setupMindsetChips()

                        Log.d(TAG, "Loaded details for member: ${member.name}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing member document: ${e.message}")
                        Toast.makeText(context, "Error loading member details", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "Member document does not exist")
                    Toast.makeText(context, "Member not found", Toast.LENGTH_SHORT).show()
                }

                // Hide loading state
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching member details: ${e.message}")
                Toast.makeText(context, "Failed to load member details", Toast.LENGTH_SHORT).show()

                // Hide loading state
                progressBar.visibility = View.GONE
            }
    }

    private fun displayMemberDetails(member: MemberInfo) {
        textViewName.text = member.name
        textViewRole.text = member.role
        textViewEmail.text = member.email
        textViewPhone.text = member.phone
        textViewDescription.text = member.description

        // Load image
        if (member.imageResId != 0) {
            imageViewMember.setImageResource(member.imageResId)
        } else if (member.imageUrl.isNotEmpty()) {
            // Use Glide to load from URL
            Glide.with(requireContext())
                .load(member.imageUrl)
                .placeholder(R.drawable.sakthi)
                .error(R.drawable.sakthi)
                .into(imageViewMember)
        } else {
            // Set default image
            imageViewMember.setImageResource(R.drawable.sakthi)
        }
    }

    // Mindset related functions
    private fun getMindsetKeyForMember(memberName: String): String {
        // Create a unique key for each member
        val sanitizedName = memberName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return MINDSET_PREFIX + sanitizedName
    }

    private fun loadCurrentMindset() {
        // Get the mindset key for this specific member
        val mindsetKey = getMindsetKeyForMember(memberName)

        // Retrieve the last saved mindset for this member
        currentMindset = sharedPreferences.getString(mindsetKey, "Happy") ?: "Happy"
        currentMindsetText.text = currentMindset
    }

    private fun setupMindsetChips() {
        // Clear any existing chips
        chipGroup.removeAllViews()

        // Create chips for each mindset option
        mindsetOptions.forEach { mindset ->
            val chip = Chip(requireContext())
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

        // Save the selected mindset to SharedPreferences
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
}