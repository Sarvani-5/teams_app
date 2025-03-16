package com.example.teams_app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment to display system status logs such as airplane mode and battery level changes
 */
class SystemStatusFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private val statusEntries = mutableListOf<StatusEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_system_status, container, false)

        recyclerView = view.findViewById(R.id.statusRecyclerView)
        emptyView = view.findViewById(R.id.emptyStatusView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        loadStatusEntries()

        return view
    }

    private fun loadStatusEntries() {
        statusEntries.clear()

        val statusPrefs = requireContext().getSharedPreferences("StatusLog", Context.MODE_PRIVATE)
        val allEntries = statusPrefs.all

        if (allEntries.isEmpty()) {
            showEmptyView(true)
            return
        }

        // Convert SharedPreferences entries to StatusEntry objects
        allEntries.forEach { (key, value) ->
            if (key.startsWith("status_")) {
                val timestamp = key.removePrefix("status_").toLongOrNull() ?: 0L
                val statusData = value.toString().split("|")

                if (statusData.size >= 2) {
                    val eventType = statusData[0]
                    val status = statusData[1]

                    statusEntries.add(
                        StatusEntry(
                            timestamp = timestamp,
                            eventType = eventType,
                            status = status
                        )
                    )
                }
            }
        }

        // Sort entries by timestamp (most recent first)
        statusEntries.sortByDescending { it.timestamp }

        if (statusEntries.isEmpty()) {
            showEmptyView(true)
        } else {
            showEmptyView(false)
            recyclerView.adapter = StatusAdapter(statusEntries)
        }
    }

    private fun showEmptyView(show: Boolean) {
        if (show) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Data class to represent a status entry
     */
    data class StatusEntry(
        val timestamp: Long,
        val eventType: String,
        val status: String
    ) {
        fun getFormattedTime(): String {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }
    }

    /**
     * Adapter for displaying status entries in a RecyclerView
     */
    inner class StatusAdapter(private val entries: List<StatusEntry>) :
        RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val eventTypeText: TextView = view.findViewById(R.id.eventTypeText)
            val statusText: TextView = view.findViewById(R.id.statusText)
            val timestampText: TextView = view.findViewById(R.id.timestampText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_status_entry, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]

            holder.eventTypeText.text = entry.eventType
            holder.statusText.text = entry.status
            holder.timestampText.text = entry.getFormattedTime()

            // Set a different background color for different event types
            val bgColor = when (entry.eventType) {
                "Airplane Mode" -> R.color.airplane_mode_bg
                "Battery Level" -> R.color.battery_level_bg
                else -> R.color.default_status_bg
            }

            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(requireContext(), bgColor)
            )
        }

        override fun getItemCount() = entries.size
    }
}