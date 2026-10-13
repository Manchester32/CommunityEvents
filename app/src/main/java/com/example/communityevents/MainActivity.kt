package com.example.communityevents

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var events: LinearLayout
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        events = findViewById(R.id.eventsContainer)
        status = findViewById(R.id.statusText)
        findViewById<Button>(R.id.addEventButton).setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
        findViewById<Button>(R.id.apiButton).setOnClickListener { testApi(false) }
        findViewById<Button>(R.id.localApiButton).setOnClickListener { testApi(true) }
    }
    override fun onResume() { super.onResume(); loadEvents() }

    private fun loadEvents() {
        events.removeAllViews()
        if (!FirebaseHelper.setup(this)) {
            status.text = "Set up Firebase in strings.xml to load and save online events. REST API button works separately."
            return
        }
        status.text = "Loading events from Firestore..."
        FirebaseHelper.db().collection("events").get()
            .addOnSuccessListener { snapshot ->
                events.removeAllViews()
                if (snapshot.isEmpty) status.text = "No events yet. Tap Add Community Event."
                else status.text = "${snapshot.size()} event(s) loaded from Cloud Firestore."
                snapshot.documents.forEach { doc ->
                    val title = doc.getString("title") ?: "Untitled event"
                    val date = doc.getString("date") ?: ""
                    val location = doc.getString("location") ?: ""
                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(20, 16, 20, 16)
                        setBackgroundResource(R.drawable.panel)
                        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        params.bottomMargin = 16
                        layoutParams = params
                    }
                    card.addView(TextView(this).apply { text = title; textSize = 20f; setTextColor(Color.rgb(18, 79, 73)) })
                    card.addView(TextView(this).apply { text = "📅 $date     📍 $location"; textSize = 14f })
                    card.addView(Button(this).apply {
                        text = "View details"; isAllCaps = false
                        setOnClickListener {
                            startActivity(Intent(this@MainActivity, DetailActivity::class.java).putExtra("eventId", doc.id))
                        }
                    })
                    events.addView(card)
                }
            }.addOnFailureListener { status.text = "Could not load events: ${it.localizedMessage}" }
    }

    private fun testApi(useLocalApi: Boolean) {
        val result = findViewById<TextView>(R.id.apiResult)
        val address = if (useLocalApi) {
            // 10.0.2.2 means the host computer when using the standard Android emulator.
            // For a physical phone, replace this with your PC's local network IPv4 address.
            "http://10.0.2.2:5092/api/events"
        } else {
            "https://jsonplaceholder.typicode.com/posts/1"
        }
        result.text = if (useLocalApi) "Connecting to your Visual Studio API..." else "Loading public REST API..."
        Thread {
            var connection: HttpURLConnection? = null
            try {
                connection = URL(address).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val code = connection.responseCode
                if (code !in 200..299) throw Exception("HTTP $code")
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val message = if (useLocalApi) {
                    val arr = org.json.JSONArray(response)
                    if (arr.length() > 0) {
                        val item = arr.getJSONObject(0)
                        "Local API success (HTTP $code): ${arr.length()} event(s). First: ${item.getString("title")} in ${item.getString("location")}"
                    } else "Local API success (HTTP $code): no events yet."
                } else {
                    val json = JSONObject(response)
                    "Public API success (HTTP $code): post #${json.getInt("id")} - ${json.getString("title")}"
                }
                runOnUiThread { result.text = message }
            } catch (e: Exception) {
                val hint = if (useLocalApi) "Run the Visual Studio API on port 5092 and use an Android emulator." else "Check internet access."
                runOnUiThread { result.text = "REST API error: ${e.localizedMessage}. $hint" }
            } finally { connection?.disconnect() }
        }.start()
    }
}
