package com.example.geojournal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_table ORDER BY date DESC")
    fun getAllJournals(): Flow<List<JournalEntity>>

    // --- TAMBAHAN UNTUK EDIT ---
    @Query("SELECT * FROM journal_table WHERE id = :id")
    suspend fun getJournalById(id: Int): JournalEntity?
    // ---------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity)

    @Delete
    suspend fun deleteJournal(journal: JournalEntity)
}