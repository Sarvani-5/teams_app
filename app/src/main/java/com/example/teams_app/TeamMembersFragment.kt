package com.example.teams_app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teams_app.adapter.TeamMembersAdapter
import com.example.teams_app.MemberInfo
import com.google.firebase.firestore.FirebaseFirestore

class TeamMembersFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: TeamMembersAdapter
    private val membersList = mutableListOf<MemberInfo>()

    companion object {
        private const val TAG = "TeamMembersFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team_members, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewTeamMembers)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = TeamMembersAdapter(membersList) { member ->
            // Handle item click - navigate to detail view
            val detailFragment = TeamMemberDetailFragment.newInstance(member.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = adapter

        fetchTeamMembersFromFirestore()

        return view
    }

    private fun fetchTeamMembersFromFirestore() {
        val firestore = FirebaseFirestore.getInstance()

        // Show loading state
        progressBar.visibility = View.VISIBLE

        firestore.collection("team_members")
            .get()
            .addOnSuccessListener { documents ->
                membersList.clear()

                if (documents.isEmpty) {
                    Log.d(TAG, "No team members found in Firestore")
                    Toast.makeText(context, "No team members found", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
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
                            membersList.add(member)
                            Log.d(TAG, "Loaded member: ${member.name}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing member document: ${e.message}")
                        }
                    }

                    // Update adapter with the new data
                    adapter.notifyDataSetChanged()
                    Log.d(TAG, "Loaded ${membersList.size} team members")
                }

                // Hide loading state
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching team members: ${e.message}")
                Toast.makeText(context, "Failed to load team members", Toast.LENGTH_SHORT).show()

                // Hide loading state
                progressBar.visibility = View.GONE
            }
    }
}