package com.example.teams_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class TeamMembersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_members, container, false)
        setupMemberCards(view)
        return view
    }

    private fun setupMemberCards(view: View) {
        view.findViewById<CardView>(R.id.member1Card).setOnClickListener {
            navigateToMemberDetail(
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
            )
        }

        view.findViewById<CardView>(R.id.member2Card).setOnClickListener {
            navigateToMemberDetail(
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
            )
        }

        view.findViewById<CardView>(R.id.member3Card).setOnClickListener {
            navigateToMemberDetail(
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
        }
    }

    private fun navigateToMemberDetail(
        name: String,
        role: String,
        email: String,
        phone: String,
        description: String,
        imageResId: Int
    ) {
        val intent = Intent(activity, MemberDetailActivity::class.java).apply {
            putExtra("MEMBER_NAME", name)
            putExtra("MEMBER_ROLE", role)
            putExtra("MEMBER_EMAIL", email)
            putExtra("MEMBER_PHONE", phone)
            putExtra("MEMBER_DESCRIPTION", description)
            putExtra("MEMBER_IMAGE", imageResId)
        }
        startActivity(intent)
    }
}
