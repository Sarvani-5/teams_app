package com.example.teams_app

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeamMemberRepository {
    companion object {
        private const val TAG = "TeamMemberRepository"
    }

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val membersCollection = db.collection("team_members")

    // Get all team members
    suspend fun getAllTeamMembers(): List<MemberInfo> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting all team members from Firestore")
        try {
            val snapshot = membersCollection.get().await()
            Log.d(TAG, "Retrieved ${snapshot.documents.size} documents from Firestore")

            return@withContext snapshot.documents.mapNotNull { doc ->
                try {
                    val member = doc.toObject(MemberInfo::class.java)?.copy(id = doc.id)
                    Log.d(TAG, "Successfully mapped document ${doc.id} to member: ${member?.name}")
                    member
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document ${doc.id}: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting team members: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    // Get a single team member by ID
    suspend fun getTeamMemberById(id: String): MemberInfo? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting team member with ID: $id")
        try {
            val document = membersCollection.document(id).get().await()
            val member = document.toObject(MemberInfo::class.java)?.copy(id = document.id)
            if (member != null) {
                Log.d(TAG, "Successfully retrieved member: ${member.name}")
            } else {
                Log.d(TAG, "No member found with ID: $id")
            }
            return@withContext member
        } catch (e: Exception) {
            Log.e(TAG, "Error getting team member $id: ${e.message}", e)
            return@withContext null
        }
    }

    // Add a new team member
    suspend fun addTeamMember(member: MemberInfo, imageUri: Uri? = null): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Adding new team member: ${member.name}")
        try {
            // If an image is provided, upload it first
            val imageUrl = if (imageUri != null) {
                Log.d(TAG, "Image provided, uploading to Firebase Storage")
                uploadImage(imageUri, member.name)
            } else {
                Log.d(TAG, "No image provided")
                ""
            }

            // Create a copy of the member with the image URL
            val memberWithImage = if (imageUrl.isNotEmpty()) {
                Log.d(TAG, "Setting image URL: $imageUrl")
                member.copy(imageUrl = imageUrl)
            } else {
                member
            }

            // Add to Firestore
            Log.d(TAG, "Adding member to Firestore collection")
            val result = membersCollection.add(memberWithImage).await()
            Log.d(TAG, "Successfully added member with ID: ${result.id}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding team member: ${e.message}", e)
            return@withContext false
        }
    }

    // Update an existing team member
    suspend fun updateTeamMember(member: MemberInfo, imageUri: Uri? = null): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Updating team member: ${member.name}, ID: ${member.id}")
        try {
            if (member.id.isEmpty()) {
                Log.e(TAG, "Cannot update member without ID")
                return@withContext false
            }

            // If a new image is provided, upload it
            val imageUrl = if (imageUri != null) {
                Log.d(TAG, "New image provided, uploading to Firebase Storage")
                uploadImage(imageUri, member.name)
            } else {
                Log.d(TAG, "Using existing image URL: ${member.imageUrl}")
                member.imageUrl
            }

            // Create updated member object
            val updatedMember = member.copy(imageUrl = imageUrl)
            Log.d(TAG, "Prepared updated member data with image URL: $imageUrl")

            // Update in Firestore
            Log.d(TAG, "Updating document in Firestore")
            membersCollection.document(member.id).set(updatedMember).await()
            Log.d(TAG, "Successfully updated member in Firestore")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating team member: ${e.message}", e)
            return@withContext false
        }
    }

    // Delete a team member
    suspend fun deleteTeamMember(id: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Deleting team member with ID: $id")
        try {
            // Get the member to find the image URL
            Log.d(TAG, "Fetching member details to check for image URL")
            val member = getTeamMemberById(id)

            // Delete the image if it exists
            if (member?.imageUrl?.isNotEmpty() == true) {
                Log.d(TAG, "Found image URL to delete: ${member.imageUrl}")
                try {
                    val storageRef = storage.getReferenceFromUrl(member.imageUrl)
                    Log.d(TAG, "Deleting image from Firebase Storage")
                    storageRef.delete().await()
                    Log.d(TAG, "Successfully deleted image from Storage")
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting image: ${e.message}", e)
                    // Continue with member deletion even if image deletion fails
                }
            }

            // Delete from Firestore
            Log.d(TAG, "Deleting member document from Firestore")
            membersCollection.document(id).delete().await()
            Log.d(TAG, "Successfully deleted member from Firestore")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting team member: ${e.message}", e)
            return@withContext false
        }
    }

    // Helper method to upload image to Firebase Storage
    private suspend fun uploadImage(imageUri: Uri, memberName: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Uploading image for member: $memberName")
        try {
            val filename = "team_members/${System.currentTimeMillis()}_${memberName.replace(" ", "_")}.jpg"
            Log.d(TAG, "Generated filename: $filename")

            val storageRef = storage.reference.child(filename)
            Log.d(TAG, "Starting upload task")

            val uploadTask = storageRef.putFile(imageUri).await()
            Log.d(TAG, "Upload completed, getting download URL")

            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "Image uploaded successfully, URL: $downloadUrl")

            return@withContext downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            return@withContext ""
        }
    }
}