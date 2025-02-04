package com.example.teams_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

class TeamMembersFragment : Fragment() {
    // Data class to store member information
    data class MemberInfo(
        val name: String,
        val role: String,
        val email: String,
        val phone: String,
        val description: String,
        val imageResId: Int
    )

    // List of team members
    private val teamMembers = listOf(
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
            R.drawable.sakthi
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
            R.drawable.yogee
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
            R.drawable.meena
        )
    )

    // Currently selected member for context menu
    private var currentSelectedMember: MemberInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_members, container, false)
        setupMemberCards(view)
        setupPopupMenuButtons(view)
        setupContextMenus(view)
        return view
    }

    private fun setupMemberCards(view: View) {
        val cardIds = listOf(R.id.member1Card, R.id.member2Card, R.id.member3Card)

        cardIds.forEachIndexed { index, cardId ->
            view.findViewById<CardView>(cardId).apply {
                // Regular click to navigate to details
                setOnClickListener {
                    navigateToMemberDetail(teamMembers[index])
                }
            }
        }
    }

    private fun setupPopupMenuButtons(view: View) {
        val popupButtonIds = listOf(
            R.id.member1PopupMenuButton,
            R.id.member2PopupMenuButton,
            R.id.member3PopupMenuButton
        )

        popupButtonIds.forEachIndexed { index, buttonId ->
            view.findViewById<ImageView>(buttonId).setOnClickListener { v ->
                val popupMenu = PopupMenu(requireContext(), v)
                popupMenu.menuInflater.inflate(R.menu.team_member_popup_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.popup_view_details -> {
                            navigateToMemberDetail(teamMembers[index])
                            true
                        }
                        R.id.popup_contact -> {
                            showContactOptions(teamMembers[index])
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

        contextCardIds.forEachIndexed { index, cardId ->
            val card = view.findViewById<CardView>(cardId)
            registerForContextMenu(card)

            // Set up long click listener to prepare context menu data
            card.setOnLongClickListener {
                currentSelectedMember = teamMembers[index]
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
            else -> super.onContextItemSelected(item)
        }
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