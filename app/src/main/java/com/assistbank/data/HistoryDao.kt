package com.assistbank.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC LIMIT 5")
    fun getLast5History(): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history_table WHERE id NOT IN (SELECT id FROM history_table ORDER BY timestamp DESC LIMIT 5)")
    suspend fun cleanupOldHistory()
}
