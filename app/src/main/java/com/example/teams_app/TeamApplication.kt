package com.example.teams_app

import android.app.Application
import android.util.Log
import com.example.teams_app.util.DataMigrationUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TeamApplication : Application() {
    companion object {
        private const val TAG = "TeamApplication"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Initializing Firebase...")

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")

            // Configure Firestore settings
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .build()
            firestore.firestoreSettings = settings
            Log.d(TAG, "Firestore settings configured with persistence enabled")

            // Initialize data migration in a coroutine
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Starting data migration check...")
                    DataMigrationUtil.checkAndMigrateData(applicationContext, firestore)
                    Log.d(TAG, "Data migration check completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during data migration: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}", e)
        }
    }
}
