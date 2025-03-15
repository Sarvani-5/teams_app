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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
            else -> super.onContextItemSelected(item)
        }
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

    // Variables to store pending SMS info for after permission is granted
    private var pendingSmsMessage: String? = null
    private var pendingSmsMember: MemberInfo? = null
    private val SMS_PERMISSION_REQUEST_CODE = 101

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
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
}