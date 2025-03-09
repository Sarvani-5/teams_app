package com.example.teams_app.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.teams_app.R
import com.example.teams_app.MemberInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DataMigrationUtil {
    private const val TAG = "DataMigrationUtil"
    private const val PREFS_NAME = "team_app_prefs"
    private const val KEY_DATA_MIGRATED = "data_migrated_to_firebase"

    suspend fun checkAndMigrateData(context: Context, firestore: FirebaseFirestore) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.getBoolean(KEY_DATA_MIGRATED, false)) {
            Log.d(TAG, "First run detected, checking if migration needed...")

            // Check if data already exists in Firestore
            val collection = firestore.collection("team_members")
            val documents = try {
                Log.d(TAG, "Checking for existing data in Firestore...")
                collection.get().await()
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Firestore data: ${e.message}", e)
                null
            }

            if (documents != null && documents.isEmpty) {
                Log.d(TAG, "No existing data found in Firestore, starting migration...")
                migrateHardcodedDataToFirestore(firestore)

                // Mark migration as completed
                prefs.edit().putBoolean(KEY_DATA_MIGRATED, true).apply()
                Log.d(TAG, "Data migration completed and marked as done")
            } else {
                Log.d(TAG, "Data already exists in Firestore or couldn't check, skipping migration")
                // Mark as migrated anyway to avoid checking again
                prefs.edit().putBoolean(KEY_DATA_MIGRATED, true).apply()
            }
        } else {
            Log.d(TAG, "Data already migrated, skipping")
        }
    }

    private suspend fun migrateHardcodedDataToFirestore(firestore: FirebaseFirestore) {
        Log.d(TAG, "Migrating hardcoded team members to Firestore...")

        val hardcodedMembers = listOf(
            MemberInfo(
                id = "1",
                name = "Sakthi Sarvani R",
                role = "Full Stack Developer & ML Engineer",
                email = "sakthisarvani@student.tce.edu",
                phone = "+91 9751423916",
                description = """
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
                imageResId = R.drawable.sakthi
            ),
            MemberInfo(
                id = "2",
                name = "Yogeetha K",
                role = "Backend Developer & UI/UX Designer",
                email = "yogeetha@student.tce.edu",
                phone = "+91 9345682720",
                description = """
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
                imageResId = R.drawable.yogee
            ),
            MemberInfo(
                id = "3",
                name = "Sowndarya Meenakshi A",
                role = "Tech Enthusiast & Developer",
                email = "sowndarya@student.tce.edu",
                phone = "+91 8072965118",
                description = """
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
                imageResId = R.drawable.meena
            )
        )

        val collection = firestore.collection("team_members")

        for (member in hardcodedMembers) {
            try {
                Log.d(TAG, "Adding team member to Firestore: ${member.name}")
                // Remove the id from the map as Firestore will generate its own
                val memberMap = mapOf(
                    "name" to member.name,
                    "role" to member.role,
                    "email" to member.email,
                    "phone" to member.phone,
                    "description" to member.description,
                    "imageUrl" to "",
                    "imageResId" to member.imageResId
                )

                collection.add(memberMap).await()
                Log.d(TAG, "Successfully added team member: ${member.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding team member ${member.name}: ${e.message}", e)
            }
        }
    }
}
