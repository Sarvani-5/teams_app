package com.example.teams_app

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritesAdapter
    private lateinit var addButton: Button
    private lateinit var emptyStateText: TextView

    private var memberName: String = ""
    private var itemType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Get data from intent
        memberName = intent.getStringExtra("MEMBER_NAME") ?: ""
        itemType = intent.getStringExtra("ITEM_TYPE") ?: ""

        // Set up toolbar
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = if (itemType == "song") "$memberName's Favorite Songs" else "$memberName's Favorite Movies"

        // Initialize views
        recyclerView = findViewById(R.id.favoritesRecyclerView)
        addButton = findViewById(R.id.addFavoriteButton)
        emptyStateText = findViewById(R.id.emptyStateText)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FavoritesAdapter(this, memberName, itemType, dbHelper)
        recyclerView.adapter = adapter

        // Set up add button
        addButton.setOnClickListener {
            showAddItemDialog()
        }

        // Load data
        loadFavorites()
    }

    private fun loadFavorites() {
        val favorites = dbHelper.getFavorites(memberName, itemType)

        if (favorites.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "No favorite ${if (itemType == "song") "songs" else "movies"} yet. Add some!"
            recyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(favorites)
        }
    }

    private fun showAddItemDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_favorite, null)
        val editText = dialogView.findViewById<EditText>(R.id.favoriteItemInput)

        builder.setView(dialogView)
            .setTitle("Add Favorite ${if (itemType == "song") "Song" else "Movie"}")
            .setPositiveButton("Add") { dialog, _ ->
                val itemName = editText.text.toString().trim()
                if (itemName.isNotEmpty()) {
                    dbHelper.addFavorite(memberName, itemType, itemName)
                    loadFavorites()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

class FavoritesAdapter(
    private val context: Context,
    private val memberName: String,
    private val itemType: String,
    private val dbHelper: DatabaseHelper
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private var favorites: List<String> = listOf()

    fun updateData(newFavorites: List<String>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favorite = favorites[position]
        holder.favoriteText.text = favorite

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete ${if (itemType == "song") "Song" else "Movie"}")
                .setMessage("Are you sure you want to delete this favorite?")
                .setPositiveButton("Yes") { _, _ ->
                    dbHelper.deleteFavorite(memberName, itemType, favorite)
                    val updatedFavorites = favorites.toMutableList()
                    updatedFavorites.removeAt(position)
                    favorites = updatedFavorites
                    notifyDataSetChanged()

                    // Check if list is now empty
                    if (favorites.isEmpty()) {
                        (context as FavoritesActivity).findViewById<TextView>(R.id.emptyStateText).visibility = View.VISIBLE
                        context.findViewById<RecyclerView>(R.id.favoritesRecyclerView).visibility = View.GONE
                        context.findViewById<TextView>(R.id.emptyStateText).text =
                            "No favorite ${if (itemType == "song") "songs" else "movies"} yet. Add some!"
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount(): Int = favorites.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val favoriteText: TextView = itemView.findViewById(R.id.favoriteText)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }
}