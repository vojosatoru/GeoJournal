package com.example.geojournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi Database
    private val database = JournalDatabase.getDatabase(application)
    private val dao = database.journalDao()

    // Data Utama: List Jurnal yang selalu update otomatis (Flow)
    val allJournals: StateFlow<List<JournalEntity>> = dao.getAllJournals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Fungsi Tambah Data
    fun addJournal(title: String, desc: String, photoUri: String, lat: Double?, lon: Double?) {
        viewModelScope.launch {
            val journal = JournalEntity(
                title = title,
                description = desc,
                photoUri = photoUri,
                latitude = lat,
                longitude = lon,
                locationName = "Lokasi Tersimpan" // Nanti bisa diganti Geocoder
            )
            dao.insertJournal(journal)
        }
    }

    // Fungsi Hapus Data
    fun deleteJournal(journal: JournalEntity) {
        viewModelScope.launch {
            dao.deleteJournal(journal)
        }
    }
}