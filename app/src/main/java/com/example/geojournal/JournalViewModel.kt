package com.example.geojournal

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JournalDatabase.getDatabase(application)
    private val dao = database.journalDao()

    // State untuk query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Stream Data Utama: Berubah real-time saat query berubah
    val allJournals: StateFlow<List<JournalEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                dao.getAllJournals()
            } else {
                dao.searchJournals(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- FITUR BARU: STATISTIK REAL-TIME (TIER S) ---

    // Hitung Total Kata (Words)
    val totalWords: StateFlow<Int> = allJournals.map { list ->
        list.sumOf { journal ->
            // Hitung kata di deskripsi (split by spasi/enter)
            if (journal.description.isBlank()) 0
            else journal.description.trim().split("\\s+".toRegex()).size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Hitung Total Hari Unik (Days)
    val totalDays: StateFlow<Int> = allJournals.map { list ->
        list.map { journal ->
            // Format tanggal jadi yyyyMMdd agar unik per hari
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(journal.date)
        }.distinct().size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    // ------------------------------------------------

    // Fungsi untuk mengubah query pencarian
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addJournal(
        id: Int = 0,
        title: String,
        desc: String,
        photoUri: String,
        lat: Double?,
        lon: Double?
    ) {
        viewModelScope.launch {
            val addressName = if (lat != null && lon != null) {
                getAddressFromLocation(lat, lon)
            } else {
                "No Location"
            }

            val journal = JournalEntity(
                id = id,
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

    suspend fun getJournalById(id: Int): JournalEntity? {
        return dao.getJournalById(id)
    }

    fun deleteJournal(journal: JournalEntity) {
        viewModelScope.launch {
            dao.deleteJournal(journal)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getAddressFromLocation(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Prioritas nama: Jalan -> Kota -> Provinsi
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