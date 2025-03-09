package com.example.teams_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teams_app.R
import com.example.teams_app.MemberInfo

class TeamMembersAdapter(
    private val members: List<MemberInfo>,
    private val onItemClick: (MemberInfo) -> Unit
) : RecyclerView.Adapter<TeamMembersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewMember)
        val textName: TextView = view.findViewById(R.id.textViewName)
        val textRole: TextView = view.findViewById(R.id.textViewRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = members[position]

        holder.textName.text = member.name
        holder.textRole.text = member.role

        // Load image
        if (member.imageResId != 0) {
            holder.imageView.setImageResource(member.imageResId)
        } else if (member.imageUrl.isNotEmpty()) {
            // Use Glide or Picasso here to load from URL
            // Example with Glide:
            // Glide.with(holder.imageView).load(member.imageUrl).into(holder.imageView)
        } else {
            // Set default image
            holder.imageView.setImageResource(R.drawable.sakthi)
        }

        holder.itemView.setOnClickListener {
            onItemClick(member)
        }
    }

    override fun getItemCount() = members.size
}
