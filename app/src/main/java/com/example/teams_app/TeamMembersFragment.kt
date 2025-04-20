package com.example.teams_app

import android.content.Context
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeamMembersFragment : Fragment() {
    // Data class to store member information
    data class MemberInfo(
        val name: String = "",
        val role: String = "",
        val email: String = "",
        val phone: String = "",
        val description: String = "",
        val imageResId: Int = 0,
        val displayOrder: Int = 0  // Add display order field
    )

    // List to store team members fetched from Firestore
    private var teamMembers = mutableListOf<MemberInfo>()

    // Firestore instance
    private lateinit var db: FirebaseFirestore

    // Currently selected member for context menu
    private var currentSelectedMember: MemberInfo? = null

    // Profile picture update variables
    private var selectedMemberIndex: Int = -1
    private lateinit var photoFile: File
    private var photoUri: Uri? = null

    // Permission request constants
    private val SMS_PERMISSION_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_REQUEST_CODE = 102
    private val GALLERY_PERMISSION_REQUEST_CODE = 103

    // Variables to store pending SMS info for after permission is granted
    private var pendingSmsMessage: String? = null
    private var pendingSmsMember: MemberInfo? = null

    // Register activity result launchers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            saveProfilePicture(photoUri!!, selectedMemberIndex)
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            saveProfilePicture(it, selectedMemberIndex)
        } ?: run {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_members, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Check if data exists in Firestore, if not add sample data
        checkAndInitializeData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // After the view is created and team members are loaded, update profile pics
        teamMembers.forEach { member ->
            updateMemberProfilePic(member.name)
        }
    }

    private fun checkAndInitializeData() {
        val teamCollection = db.collection("team_members")

        teamCollection.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No data exists, store initial data
                    storeInitialTeamData()
                } else {
                    // Data exists, just fetch it
                    fetchTeamData()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TeamMembersFragment", "Error checking for data: ", exception)
                Toast.makeText(context, "Failed to check database: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeInitialTeamData() {
        val initialMembers = listOf(
            MemberInfo(
                "Sakthi Sarvani R",
                "Full Stack Developer & ML Engineer",
                "sakthisarvani@student.tce.edu",
                "+91 9751423916",
                """
                🚀 Passionate Full Stack Developer & ML Engineer with expertise in web development, 
                machine learning applications, and virtual reality development. Always eager to explore 
                cutting-edge technologies and contribute innovative solutions.  
                
                🔹 Skills & Expertise:  
                - 💻 Full Stack Development  
                - 🤖 Machine Learning & AI  
                - 🎨 UI/UX Implementation  
                - 🔗 API Integration & Backend Development  
                - 🗄️ Database Management  
                - 🕶️ Virtual Reality Development (Unity 3D & AR/VR)  
                """.trimIndent(),
                R.drawable.sakthi,
                1  // First in display order
            ),
            MemberInfo(
                "Yogeetha K",
                "Backend Developer & UI/UX Designer",
                "yogeetha@student.tce.edu",
                "+91 9345682720",
                """
                🚀 Passionate backend developer with expertise in both server-side 
                development and UI/UX design. Combines technical skills with creativity 
                to build efficient, scalable, and user-friendly applications.
                
                🔹 Skills & Expertise:  
                - 🖥️ Backend Development  
                - 🎨 UI/UX Design  
                - 📱 Mobile App Development  
                - 🧠 Problem-Solving  
                - ⚽ Athletic Skills  
                - 🎨 Painting & Creativity  
                """.trimIndent(),
                R.drawable.yogee,
                2  // Second in display order
            ),
            MemberInfo(
                "Sowndarya Meenakshi A",
                "Tech Enthusiast & Developer",
                "sowndarya@student.tce.edu",
                "+91 8072965118",
                """
                🚀Tech enthusiast with a passion for AI, encryption, web development, 
                and big data. Focused on solving real-world problems through innovative 
                solutions, including intelligent ML models, secure encryption techniques, 
                and dynamic web applications.  

                🔹 Skills & Expertise: 
                - 🤖 Machine Learning & AI – Expertise in computer vision, pose estimation, 
                  and NLP using tools like TensorFlow and OpenCV.  
                - 🌐 Web Development – Experience in full-stack development with React.js, 
                  Flask, and SQL for interactive applications.  
                - 🎮 Game Development – Hands-on experience with Unity for immersive 
                  experiences.  
                """.trimIndent(),
                R.drawable.meena,
                3  // Third in display order
            )
        )

        // Add each member to Firestore
        initialMembers.forEach { member ->
            val memberData = hashMapOf(
                "name" to member.name,
                "role" to member.role,
                "email" to member.email,
                "phone" to member.phone,
                "description" to member.description,
                "imageResId" to member.imageResId,
                "displayOrder" to member.displayOrder
            )

            db.collection("team_members").add(memberData)
                .addOnSuccessListener { documentReference ->
                    Log.d("TeamMembersFragment", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("TeamMembersFragment", "Error adding document", e)
                }
        }

        // After storing data, fetch it to display
        fetchTeamData()
    }

    private fun fetchTeamData() {
        db.collection("team_members").get()
            .addOnSuccessListener { result ->
                teamMembers.clear()
                for (document in result) {
                    val member = MemberInfo(
                        name = document.getString("name") ?: "",
                        role = document.getString("role") ?: "",
                        email = document.getString("email") ?: "",
                        phone = document.getString("phone") ?: "",
                        description = document.getString("description") ?: "",
                        imageResId = document.getLong("imageResId")?.toInt() ?: 0,
                        displayOrder = document.getLong("displayOrder")?.toInt() ?: Int.MAX_VALUE
                    )
                    teamMembers.add(member)
                }

                // Sort by display order
                teamMembers.sortBy { it.displayOrder }

                // Now that we have sorted data, setup the UI
                view?.let { setupUI(it) }
            }
            .addOnFailureListener { exception ->
                Log.e("TeamMembersFragment", "Error getting documents: ", exception)
                Toast.makeText(context, "Failed to load team data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupUI(view: View) {
        setupMemberCards(view)
        setupPopupMenuButtons(view)
        setupContextMenus(view)

        // Update profile pictures
        teamMembers.forEach { member ->
            updateMemberProfilePic(member.name)
        }
    }

    private fun setupMemberCards(view: View) {
        val cardIds = listOf(R.id.member1Card, R.id.member2Card, R.id.member3Card)

        // Make sure we don't try to access non-existent elements
        val memberCount = minOf(teamMembers.size, cardIds.size)

        for (i in 0 until memberCount) {
            view.findViewById<CardView>(cardIds[i]).apply {
                // Show the card (in case it was hidden)
                visibility = View.VISIBLE

                // Regular click to navigate to details
                setOnClickListener {
                    navigateToMemberDetail(teamMembers[i])
                }
            }
        }

        // Hide any unused cards
        for (i in memberCount until cardIds.size) {
            view.findViewById<CardView>(cardIds[i]).visibility = View.GONE
        }
    }

    private fun setupPopupMenuButtons(view: View) {
        val popupButtonIds = listOf(
            R.id.member1PopupMenuButton,
            R.id.member2PopupMenuButton,
            R.id.member3PopupMenuButton
        )

        // Make sure we don't try to access non-existent elements
        val memberCount = minOf(teamMembers.size, popupButtonIds.size)

        for (i in 0 until memberCount) {
            view.findViewById<ImageView>(popupButtonIds[i]).setOnClickListener { v ->
                val popupMenu = PopupMenu(requireContext(), v)
                popupMenu.menuInflater.inflate(R.menu.team_member_popup_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.popup_view_details -> {
                            navigateToMemberDetail(teamMembers[i])
                            true
                        }
                        R.id.popup_contact -> {
                            showContactOptions(teamMembers[i])
                            true
                        }
                        R.id.popup_change_pic -> {
                            selectedMemberIndex = i
                            showChangeProfilePicOptions(teamMembers[i])
                            true
                        }
                        else -> false
                    }
                }

                popupMenu.show()
            }
        }
    }

    private fun setupContextMenus(view: View) {
        val contextCardIds = listOf(R.id.member1Card, R.id.member2Card, R.id.member3Card)

        // Make sure we don't try to access non-existent elements
        val memberCount = minOf(teamMembers.size, contextCardIds.size)

        for (i in 0 until memberCount) {
            val card = view.findViewById<CardView>(contextCardIds[i])
            registerForContextMenu(card)

            // Set up long click listener to prepare context menu data
            card.setOnLongClickListener {
                currentSelectedMember = teamMembers[i]
                false  // Return false to allow context menu to be shown
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.team_member_context_menu, menu)
    }

    private fun showContactOptions(member: MemberInfo) {
        val options = arrayOf("Send Email", "Make Phone Call")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Contact ${member.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sendEmail(member.email)
                    1 -> makePhoneCall(member.phone)
                }
            }
            .show()
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Contact from Team App")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No phone app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val member = currentSelectedMember ?: return false

        return when (item.itemId) {
            R.id.context_send_email -> {
                sendEmail(member.email)
                true
            }
            R.id.context_call -> {
                makePhoneCall(member.phone)
                true
            }
            R.id.context_send_sms -> {
                sendSmsAlert(member)
                true
            }
            R.id.context_send_whatsapp -> {
                sendWhatsAppMessage(member)
                true
            }
            R.id.context_change_pic -> {
                selectedMemberIndex = teamMembers.indexOf(member)
                showChangeProfilePicOptions(member)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // Move WhatsAppHelper functions directly into the class
    private fun sendWhatsAppMessage(member: MemberInfo) {
        // Create a dialog to compose the WhatsApp message
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_send_alert, null)

        val messageEditText = dialogView.findViewById<EditText>(R.id.alertMessageEditText)
        val msg = dialogView.findViewById<TextView>(R.id.alert)
        msg.text = "This message will be sent via Whatsapp"

        val messageTypeText = dialogView.findViewWithTag<TextView>("messageTypeText")
            ?: dialogView.findViewById<TextView>(android.R.id.text2)  // Fallback if tag not found

        // Update the info text to indicate WhatsApp
        messageTypeText?.text = "This message will be sent via WhatsApp"

        // Pre-fill message with member's name
        val defaultMessage = "Hey ${member.name}, How are you doing?"
        messageEditText.setText(defaultMessage)

        AlertDialog.Builder(requireContext())
            .setTitle("Message to ${member.name}")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val message = messageEditText.text.toString().trim()

                if (message.isNotEmpty() && member.phone.isNotEmpty()) {
                    // Send WhatsApp message
                    val success = sendWhatsAppMessageImpl(requireContext(), member.phone, message)

                    if (success) {
                        // Log the WhatsApp message
                        logWhatsAppMessageSent(requireContext(), member.name, member.phone, message)
                    }
                } else {
                    Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    // Renamed WhatsAppHelper functions to avoid conflicts
    private fun sendWhatsAppMessageImpl(context: Context, phoneNumber: String, message: String): Boolean {
        val sanitizedNumber = phoneNumber.replace(" ", "").replace("-", "")

        // Create intent with WhatsApp package
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$sanitizedNumber&text=${Uri.encode(message)}")
            setPackage("com.whatsapp")
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("TeamMembersFragment", "WhatsApp not installed or error: ${e.message}")
            Toast.makeText(context, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun logWhatsAppMessageSent(context: Context, memberName: String, phoneNumber: String, message: String) {
        val whatsappPrefs = context.getSharedPreferences("WhatsAppLog", Context.MODE_PRIVATE)
        val timestamp = System.currentTimeMillis()
        val editor = whatsappPrefs.edit()

        // Store the message details
        editor.putString("whatsapp_$timestamp", "$memberName|$phoneNumber|$message")
        editor.apply()
    }

    private fun sendSmsAlert(member: MemberInfo) {
        // Get current mindset of the member
        val currentMindset = MemberDetailActivity.getMemberMindset(requireContext(), member.name)

        // Create a dialog to compose the SMS
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_send_alert, null)

        val messageEditText = dialogView.findViewById<EditText>(R.id.alertMessageEditText)

        // Pre-fill message with member's name and current mindset
        val defaultMessage = "Hey ${member.name}, How are you doing?"
        messageEditText.setText(defaultMessage)

        AlertDialog.Builder(requireContext())
            .setTitle("Send Alert to ${member.name}")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val message = messageEditText.text.toString().trim()

                if (message.isNotEmpty() && member.phone.isNotEmpty()) {
                    // Check for SMS permission
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.SEND_SMS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Store the message and member info for after permission is granted
                        pendingSmsMessage = message
                        pendingSmsMember = member

                        // Request permission
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.SEND_SMS),
                            SMS_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        // Permission already granted, send SMS
                        sendSms(member.phone, message, member.name)
                    }
                } else {
                    Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun sendSms(phoneNumber: String, message: String, memberName: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(requireContext(), "Alert sent to $memberName", Toast.LENGTH_SHORT).show()

            // Log the alert in SharedPreferences
            logAlertSent(memberName, phoneNumber, message)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to send alert: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logAlertSent(memberName: String, phoneNumber: String, message: String) {
        val alertsPrefs = requireContext().getSharedPreferences("AlertsLog", Context.MODE_PRIVATE)
        val timestamp = System.currentTimeMillis()
        val editor = alertsPrefs.edit()

        // Store the alert details
        editor.putString("alert_$timestamp", "$memberName|$phoneNumber|$message")
        editor.apply()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, send the pending SMS
                    pendingSmsMember?.let { member ->
                        pendingSmsMessage?.let { message ->
                            sendSms(member.phone, message, member.name)

                            // Clear pending data
                            pendingSmsMessage = null
                            pendingSmsMember = null
                        }
                    }
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageLauncher.launch("image/*")
                } else {
                    Toast.makeText(requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMemberDetail(member: MemberInfo) {
        val intent = Intent(activity, MemberDetailActivity::class.java).apply {
            putExtra("MEMBER_NAME", member.name)
            putExtra("MEMBER_ROLE", member.role)
            putExtra("MEMBER_EMAIL", member.email)
            putExtra("MEMBER_PHONE", member.phone)
            putExtra("MEMBER_DESCRIPTION", member.description)
            putExtra("MEMBER_IMAGE", member.imageResId)
        }
        startActivity(intent)
    }

    // Profile picture handling functions
    private fun showChangeProfilePicOptions(member: MemberInfo) {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Change profile picture for ${member.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndTakePhoto()
                    1 -> checkGalleryPermissionAndPickImage()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request camera permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Camera permission already granted
            dispatchTakePictureIntent()
        }
    }

    private fun checkGalleryPermissionAndPickImage() {
        // For Android 10+ we don't need READ_EXTERNAL_STORAGE permission for picking images
        // For older versions, we check for permission
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted or not needed
            pickImageLauncher.launch("image/*")
        }
    }

    private fun dispatchTakePictureIntent() {
        val context = requireContext()
        // Create file for the photo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null) ?: context.filesDir

        try {
            photoFile = File.createTempFile(
                "JPEG_${timeStamp}_",  // prefix
                ".jpg",                 // suffix
                storageDir              // directory
            )

            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            // Launch camera with the URI
            takePictureLauncher.launch(photoUri)
        } catch (ex: IOException) {
            Log.e("TeamMembersFragment", "Error creating photo file", ex)
            Toast.makeText(
                context,
                "Error creating photo file: ${ex.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveProfilePicture(imageUri: Uri, memberIndex: Int) {
        if (memberIndex < 0 || memberIndex >= teamMembers.size) {
            Log.e("TeamMembersFragment", "Invalid member index: $memberIndex")
            return
        }

        val member = teamMembers[memberIndex]

        try {
            // Save image URI to SharedPreferences
            val profilePrefs = requireContext().getSharedPreferences("ProfilePictures", Context.MODE_PRIVATE)
            profilePrefs.edit().putString(member.name, imageUri.toString()).apply()

            // Update UI
            updateMemberProfilePic(member.name)

            Toast.makeText(
                requireContext(),
                "Profile picture updated for ${member.name}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e("TeamMembersFragment", "Error saving profile picture", e)
            Toast.makeText(
                requireContext(),
                "Failed to save profile picture: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateMemberProfilePic(memberName: String) {
        val view = view ?: return
        val memberIndex = teamMembers.indexOfFirst { it.name == memberName }
        if (memberIndex < 0) return

        // Get the image view based on member index
        val imageViewId = when (memberIndex) {
            0 -> R.id.member1Image
            1 -> R.id.member2Image
            2 -> R.id.member3Image
            else -> return
        }

        val imageView = view.findViewById<ImageView>(imageViewId)

        // Get saved image URI
        val profilePrefs = requireContext().getSharedPreferences("ProfilePictures", Context.MODE_PRIVATE)
        val savedImageUri = profilePrefs.getString(memberName, null)

        if (!savedImageUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(savedImageUri)
                imageView.setImageURI(null) // Clear the image view first
                imageView.setImageURI(uri)   // Set the new image
            } catch (e: Exception) {
                // If loading fails, use default image
                imageView.setImageResource(teamMembers[memberIndex].imageResId)
                Log.e("TeamMembersFragment", "Error loading profile image: ${e.message}")
            }
        } else {
            // No custom image, use default
            imageView.setImageResource(teamMembers[memberIndex].imageResId)
        }
    }

    // Override onResume to ensure profile pictures are loaded when returning to the fragment
    override fun onResume() {
        super.onResume()

        // Update all profile pictures
        teamMembers.forEach { member ->
            updateMemberProfilePic(member.name)
        }
    }
}