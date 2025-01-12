package com.azim.poli_run

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.azim.poli_run.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import android.util.Log
import com.google.firebase.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var itemAdapter: ItemAdapter
    private val db : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var dataList: MutableList<Data> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and Adapter
        itemAdapter = ItemAdapter(dataList)  // Pass the initialized list to the adapter
        binding.recylerViewData.layoutManager = LinearLayoutManager(this)
        binding.recylerViewData.adapter = itemAdapter

        // Fetch data from Firestore
        // fetchDataFromFirestore()
        fetchDataInRealTime()

        // Handle FAB click to start UploadActivity
        binding.fab.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }

    // Fetch data from Firestore in real-time
    private fun fetchDataInRealTime() {
        // Listen to changes in the "runData" collection in real-time
        db.collection("runData")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MainActivity", "Error listening to Firestore", e)
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Clear current data and parse new data
                val newDataList = mutableListOf<Data>()
                snapshots?.forEach { document ->
                    try {
                        // Parse the fields safely
                        val name = document.getString("name").orEmpty()
                        val topic = document.getString("topic").orEmpty()
                        val description = document.getString("description").orEmpty()

                        // Fetch additional fields
                        val index = document.getLong("index")?.toInt() ?: 0  // Default to 0 if not found
                        val currentMonth = document.getLong("currentMonth")?.toInt() ?: 0  // Default to 0 if not found

                        // Add parsed data to the newDataList
                        val data = Data(name = name,
                            topic = topic,
                            description = description,
                            timestamp = null,
                            currentMonth = currentMonth,
                            currentYear = null,
                            index = index)
                        newDataList.add(data)

                        Log.d("MainActivity", "Fetched Data: $topic, $description")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error parsing document data", e)
                    }
                }

                // Update the RecyclerView if new data is available
                if (newDataList.isNotEmpty()) {
                    itemAdapter.setData(newDataList)  // Notify the adapter of new data
                    Toast.makeText(this, "Data loaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No Data Available", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
