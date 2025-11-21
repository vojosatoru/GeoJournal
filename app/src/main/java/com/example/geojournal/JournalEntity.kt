package com.example.geojournal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_table")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val photoUri: String, // Lokasi file foto di HP
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = "Unknown Location",
    val date: Long = System.currentTimeMillis()
)