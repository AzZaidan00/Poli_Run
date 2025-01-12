package com.azim.poli_run

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azim.poli_run.databinding.ActivityUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar
import java.util.Date

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "UploadActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding for the activity
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle the save button click
        binding.saveButton.setOnClickListener {
            uploadDataToFirestore()
        }
    }

    private fun getUsernameFromFirestore(callback: (String) -> Unit)  {
        val currentUser = auth.currentUser

        // Assuming that the user is already logged in, so currentUser will not be null
        currentUser?.let { user ->
            // Fetch user data from Firestore
            db.collection("users") // 'users' is the collection in Firestore
                .document(user.uid) // The document ID is the user's UID
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Retrieve the user's name from Firestore (replace "name" with your field name)
                        val userName = document.getString("name")
                        callback(userName ?: "Ahmad") // userName Ahmad testing sahaja, nanti tukar Unknown
                    } else {
                        Toast.makeText(this, "No user data found",Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error if the data fetch fails
                    Toast.makeText(this, "Error fetching data",Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }
        }
    }

    private fun uploadDataToFirestore() {
        getUsernameFromFirestore { userName ->
            val name = userName
            val topic = binding.uploadTopic.text.toString().trim()
            val description = binding.uploadDesc.text.toString().trim()
            val timeStamp = com.google.firebase.Timestamp.now()

            // Validate input before proceeding
            if (topic.isEmpty() || description.isEmpty()) {
                Snackbar.make(binding.root, "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
                return@getUsernameFromFirestore
            }

            // Get current month and year for the index
            val currentCalendar = Calendar.getInstance()
            val currentMonth = currentCalendar.get(Calendar.MONTH) + 1 // Month are 0-based in Calendar
            val currentYear = currentCalendar.get(Calendar.YEAR)

            // Reference to the 'index' document
            val indexRef = db.collection("index")
                .document("monthlyData")

            // Attempt to retrieve the document
            indexRef.get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Initialize the index with 1 for the current month and year if the document does not exist
                        val initialIndex = hashMapOf<String, Any>(
                            "$currentYear-$currentMonth" to 1
                        )
                        indexRef.set(initialIndex, SetOptions.merge()) // Set document with a merged option (prevents overwriting)
                            .addOnSuccessListener {
                                Log.d(TAG, "Index data initialized for $currentMonth/$currentYear")
                                // Retry upload after initialization
                                retryUploadData(name, topic, description, timeStamp, currentMonth, currentYear)
                            }
                            .addOnFailureListener { ex ->
                                Log.e(TAG, "Failed to initialize index data", ex)
                                Snackbar.make(binding.root, "Failed to initialize index", Snackbar.LENGTH_LONG).show()
                            }
                    } else {
                        // If document exists, proceed with the index update
                        retryUploadData(name, topic, description, timeStamp, currentMonth, currentYear)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error retrieving index data", e)
                    Snackbar.make(binding.root, "Failed to retrieve index", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    // Function to retry the upload data after ensuring the document is created
    private fun retryUploadData(name: String, topic: String, description: String, timeStamp: com.google.firebase.Timestamp, currentMonth: Int, currentYear: Int) {
        val indexRef = db.collection("index").document("monthlyData")
        indexRef.get()
            .addOnSuccessListener { document ->
                // Retrieve the current index value or initialize it to 1 if it doesn't exist
                val currentIndex = document.getLong("$currentYear-$currentMonth")?.toInt() ?: 1

                // Create a new data object with the count as index
                val newData = Data(name = name,
                    topic = topic,
                    description = description,
                    timestamp = timeStamp,
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    index = currentIndex)

                Log.d(TAG, "Attempting to add document with data: $newData")
                db.collection("runData")
                    .add(newData)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                        Snackbar.make(binding.root, "Upload Successful", Snackbar.LENGTH_LONG).show()

                        // Update the index in the Firestore document for future uploads
                        updateMonthIndex(currentMonth, currentYear)

                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding document", e)
                        Snackbar.make(binding.root, "Upload Failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error retrieving index data", e)
                Snackbar.make(binding.root, "Failed to retrieve index", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun updateMonthIndex(currentMonth: Int, currentYear: Int) {
        // Increment the index for the current month and year
        val indexRef = db.collection("index").document("monthlyData")
        indexRef.update("$currentYear-$currentMonth", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d(TAG, "Index updated successfully for $currentMonth/$currentYear")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating index", e)
            }
    }

}