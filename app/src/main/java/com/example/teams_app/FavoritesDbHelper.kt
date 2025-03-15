package com.example.teams_app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoritesDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Favorites.db"

        // Table name
        const val TABLE_FAVORITES = "favorites"

        // Column names
        const val COLUMN_ID = "id"
        const val COLUMN_MEMBER_NAME = "member_name"
        const val COLUMN_TYPE = "type" // "song" or "movie"
        const val COLUMN_NAME = "name" // song name or movie name
        const val COLUMN_EXTRA = "extra" // genre for songs, language for movies
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_FAVORITES_TABLE = ("CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEMBER_NAME + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EXTRA + " TEXT" + ")")
        db.execSQL(CREATE_FAVORITES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    // Add a new favorite (song or movie)
    fun addFavorite(memberName: String, type: String, name: String, extra: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MEMBER_NAME, memberName)
            put(COLUMN_TYPE, type)
            put(COLUMN_NAME, name)
            put(COLUMN_EXTRA, extra)
        }
        val id = db.insert(TABLE_FAVORITES, null, values)
        db.close()
        return id
    }

    // Get all favorites of a specific type for a member
    fun getFavorites(memberName: String, type: String): List<Favorite> {
        val favoriteList = mutableListOf<Favorite>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_FAVORITES WHERE $COLUMN_MEMBER_NAME = ? AND $COLUMN_TYPE = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(memberName, type))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val extra = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRA))

                val favorite = Favorite(id, memberName, type, name, extra)
                favoriteList.add(favorite)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return favoriteList
    }

    // Delete a favorite by ID
    fun deleteFavorite(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_FAVORITES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}

// Data class to represent a favorite item
data class Favorite(
    val id: Int,
    val memberName: String,
    val type: String,
    val name: String,
    val extra: String
)