package com.azim.poli_run

import com.google.firebase.Timestamp
import java.time.Month
import java.time.Year

/**
 * Data class to represent an item with a topic, description, and language.
 * @param topic The title of the item.
 * @param description A brief description of the item.
 * @param language The language associated with the item.
 */
data class Data(
    val name : String = "",
    val topic: String = "",
    val description: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val currentMonth: Int? = null,
    val currentYear: Int? = null,
    val index: Int? = null
) {
    init {
        // Optional: Add some basic validation
        require(topic != null && topic.isNotBlank()) { "Topic cannot be empty" }
        require(description != null && description.isNotBlank()) { "Description cannot be empty" }
    }
}
