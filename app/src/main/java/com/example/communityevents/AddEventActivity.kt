package com.example.communityevents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class AddEventActivity : AppCompatActivity() {
    private var selectedImage: Uri? = null
    private lateinit var status: TextView
    private lateinit var saveButton: Button
    private val imagePicker = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        status = findViewById(R.id.addStatus)
        saveButton = findViewById(R.id.saveButton)
        findViewById<Button>(R.id.pickImageButton).setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }, imagePicker)
        }
        saveButton.setOnClickListener { saveEvent() }
    }

    @Deprecated("Uses simple image picker compatible with the class project")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePicker && resultCode == RESULT_OK) {
            selectedImage = data?.data
            selectedImage?.let {
                findViewById<ImageView>(R.id.imagePreview).apply { visibility = View.VISIBLE; setImageURI(it) }
            }
        }
    }

    private fun saveEvent() {
        val title = findViewById<EditText>(R.id.titleInput)
        val date = findViewById<EditText>(R.id.dateInput)
        val location = findViewById<EditText>(R.id.locationInput)
        val description = findViewById<EditText>(R.id.descriptionInput)
        val fields = listOf(title, date, location, description)
        val missing = fields.firstOrNull { it.text.toString().trim().isEmpty() }
        if (missing != null) { missing.error = "This field is required"; missing.requestFocus(); return }
        val dateString = date.text.toString().trim()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(dateString) ||
            try { formatter.parse(dateString) == null } catch (_: Exception) { true }) {
            date.error = "Enter a real date: yyyy-MM-dd"; date.requestFocus(); return
        }
        if (!FirebaseHelper.setup(this)) {
            status.text = "Firebase not configured. Follow README setup steps first."
            return
        }
        saveButton.isEnabled = false
        status.text = "Saving event online..."
        val data = hashMapOf<String, Any>(
            "title" to title.text.toString().trim(), "date" to dateString,
            "location" to location.text.toString().trim(),
            "description" to description.text.toString().trim()
        )
        fun writeToFirestore(imageUrl: String = "") {
            data["imageUrl"] = imageUrl
            FirebaseHelper.db().collection("events").add(data)
                .addOnSuccessListener { status.text = "Event saved successfully!"; finish() }
                .addOnFailureListener { saveButton.isEnabled = true; status.text = "Save failed: ${it.localizedMessage}" }
        }
        val uri = selectedImage
        if (uri == null) { writeToFirestore(); return }
        status.text = "Uploading image to Firebase Storage..."
        val reference = FirebaseHelper.storage().reference.child("event_images/${UUID.randomUUID()}.jpg")
        reference.putFile(uri).addOnSuccessListener {
            reference.downloadUrl.addOnSuccessListener { url -> writeToFirestore(url.toString()) }
                .addOnFailureListener { saveButton.isEnabled = true; status.text = "Image URL error: ${it.localizedMessage}" }
        }.addOnFailureListener { saveButton.isEnabled = true; status.text = "Image upload failed: ${it.localizedMessage}" }
    }
}
