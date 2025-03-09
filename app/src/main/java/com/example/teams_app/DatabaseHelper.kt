package com.example.teams_app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "TeamsDatabase.db"

        // Favorites table
        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_ID = "id"
        const val COLUMN_MEMBER_NAME = "member_name"
        const val COLUMN_ITEM_TYPE = "item_type" // "song" or "movie"
        const val COLUMN_ITEM_NAME = "item_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_FAVORITES_TABLE = ("CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEMBER_NAME + " TEXT,"
                + COLUMN_ITEM_TYPE + " TEXT,"
                + COLUMN_ITEM_NAME + " TEXT" + ")")
        db.execSQL(CREATE_FAVORITES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    // Add a new favorite item for a member
    fun addFavorite(memberName: String, itemType: String, itemName: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_MEMBER_NAME, memberName)
        values.put(COLUMN_ITEM_TYPE, itemType)
        values.put(COLUMN_ITEM_NAME, itemName)

        // Insert the new row, returning the primary key value of the new row
        val result = db.insert(TABLE_FAVORITES, null, values)
        db.close()
        return result
    }

    // Get all favorites of a specific type for a member
    fun getFavorites(memberName: String, itemType: String): List<String> {
        val favoritesList = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_FAVORITES WHERE $COLUMN_MEMBER_NAME = ? AND $COLUMN_ITEM_TYPE = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(memberName, itemType))

        if (cursor.moveToFirst()) {
            do {
                val itemNameIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME)
                if (itemNameIndex != -1) {
                    favoritesList.add(cursor.getString(itemNameIndex))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return favoritesList
    }

    // Delete a favorite item
    fun deleteFavorite(memberName: String, itemType: String, itemName: String): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_FAVORITES,
            "$COLUMN_MEMBER_NAME = ? AND $COLUMN_ITEM_TYPE = ? AND $COLUMN_ITEM_NAME = ?",
            arrayOf(memberName, itemType, itemName))
        db.close()
        return result
    }
}