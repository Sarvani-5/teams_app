package com.example.teams_app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoritesDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "Favorites.db"

        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_ID = "id"
        const val COLUMN_MEMBER_NAME = "member_name"
        const val COLUMN_TYPE = "type"
        const val COLUMN_NAME = "name"
        const val COLUMN_EXTRA = "extra"
        const val COLUMN_AUDIO_PATH = "audio_path"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_FAVORITES_TABLE = ("CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEMBER_NAME + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EXTRA + " TEXT,"
                + COLUMN_AUDIO_PATH + " TEXT"
                + ")")
        db.execSQL(CREATE_FAVORITES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_FAVORITES ADD COLUMN $COLUMN_AUDIO_PATH TEXT")
        }
    }

    fun addFavorite(
        memberName: String,
        type: String,
        name: String,
        extra: String,
        audioPath: String? = null
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MEMBER_NAME, memberName)
            put(COLUMN_TYPE, type)
            put(COLUMN_NAME, name)
            put(COLUMN_EXTRA, extra)
            put(COLUMN_AUDIO_PATH, audioPath)
        }
        val id = db.insert(TABLE_FAVORITES, null, values)
        db.close()
        return id
    }

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

                val audioPath = try {
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_PATH))
                } catch (e: Exception) {
                    null
                }

                val favorite = Favorite(id, memberName, type, name, extra, audioPath)
                favoriteList.add(favorite)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return favoriteList
    }

    fun deleteFavorite(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_FAVORITES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}

data class Favorite(
    val id: Int,
    val memberName: String,
    val type: String,
    val name: String,
    val extra: String,
    val audioPath: String? = null
)