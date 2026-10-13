package com.example.communityevents

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseHelper {
    private var ready = false
    fun setup(context: Context): Boolean {
        if (ready) return true
        val appId = context.getString(R.string.firebase_app_id)
        val apiKey = context.getString(R.string.firebase_api_key)
        val projectId = context.getString(R.string.firebase_project_id)
        val bucket = context.getString(R.string.firebase_storage_bucket)
        if (listOf(appId, apiKey, projectId, bucket).any { it.startsWith("YOUR_") || it.isBlank() }) return false
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(appId).setApiKey(apiKey).setProjectId(projectId)
                    .setStorageBucket(bucket).build()
                FirebaseApp.initializeApp(context.applicationContext, options)
            }
            ready = true
            true
        } catch (_: Exception) { false }
    }
    fun db(): FirebaseFirestore = FirebaseFirestore.getInstance()
    fun storage(): FirebaseStorage = FirebaseStorage.getInstance()
}
