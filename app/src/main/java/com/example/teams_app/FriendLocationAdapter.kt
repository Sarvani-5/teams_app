package com.example.teams_app

import LocationData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FriendLocationAdapter(
    private var locations: List<LocationData>,
    private val onItemClick: (LocationData) -> Unit
) : RecyclerView.Adapter<FriendLocationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tvName)
        val addressText: TextView = view.findViewById(R.id.tvFriendAddress)
        val timeText: TextView = view.findViewById(R.id.tvTime)
        val container: CardView = view.findViewById(R.id.cardContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_location, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]
        holder.nameText.text = location.name
        holder.addressText.text = location.address
        holder.timeText.text = formatTime(location.timestamp)
        holder.container.setOnClickListener { onItemClick(location) }
    }

    override fun getItemCount() = locations.size

    fun updateLocations(newLocations: List<LocationData>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}