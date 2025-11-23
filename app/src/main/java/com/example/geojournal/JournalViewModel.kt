package com.example.geojournal

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JournalDatabase.getDatabase(application)
    private val dao = database.journalDao()

    val allJournals: StateFlow<List<JournalEntity>> = dao.getAllJournals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Fungsi Tambah Data yang diperbarui
    fun addJournal(
        title: String,
        desc: String,
        photoUri: String,
        lat: Double?,
        lon: Double?
    ) {
        viewModelScope.launch {
            // Dapatkan nama lokasi dari koordinat (Geocoding)
            val addressName = if (lat != null && lon != null) {
                getAddressFromLocation(lat, lon)
            } else {
                "No Location"
            }

            val journal = JournalEntity(
                title = title,
                description = desc,
                photoUri = photoUri,
                latitude = lat,
                longitude = lon,
                locationName = addressName
            )
            dao.insertJournal(journal)
        }
    }

    fun deleteJournal(journal: JournalEntity) {
        viewModelScope.launch {
            dao.deleteJournal(journal)
        }
    }

    // Helper: Mengubah Lat/Long menjadi Alamat yang mudah dibaca
    @Suppress("DEPRECATION")
    private suspend fun getAddressFromLocation(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Mengambil nama jalan atau kota (Locality)
                    address.thoroughfare ?: address.locality ?: address.adminArea ?: "Unknown Location"
                } else {
                    "Unknown Location"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown Location"
            }
        }
    }
}