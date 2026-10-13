package com.example.communityevents

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
        val id = intent.getStringExtra("eventId")
        if (id.isNullOrBlank() || !FirebaseHelper.setup(this)) { finish(); return }
        FirebaseHelper.db().collection("events").document(id).get()
            .addOnSuccessListener { doc ->
                findViewById<TextView>(R.id.detailTitle).text = doc.getString("title") ?: "Event"
                findViewById<TextView>(R.id.detailDate).text = "Date: ${doc.getString("date") ?: ""}"
                findViewById<TextView>(R.id.detailLocation).text = "Location: ${doc.getString("location") ?: ""}"
                findViewById<TextView>(R.id.detailDescription).text = doc.getString("description") ?: ""
                val url = doc.getString("imageUrl") ?: ""
                if (url.isNotBlank()) {
                    val image = findViewById<ImageView>(R.id.detailImage)
                    image.visibility = View.VISIBLE
                    Glide.with(this).load(url).into(image)
                }
            }.addOnFailureListener {
                findViewById<TextView>(R.id.detailTitle).text = "Could not load this event: ${it.localizedMessage}"
            }
    }
}
