package com.example.geojournal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    // Ambil semua jurnal (Default)
    @Query("SELECT * FROM journal_table ORDER BY date DESC")
    fun getAllJournals(): Flow<List<JournalEntity>>

    // --- FITUR BARU: SEARCH ---
    // Mencari jurnal yang judul ATAU deskripsinya mengandung kata kunci
    @Query("SELECT * FROM journal_table WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchJournals(query: String): Flow<List<JournalEntity>>
    // --------------------------

    @Query("SELECT * FROM journal_table WHERE id = :id")
    suspend fun getJournalById(id: Int): JournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity)

    @Delete
    suspend fun deleteJournal(journal: JournalEntity)
}