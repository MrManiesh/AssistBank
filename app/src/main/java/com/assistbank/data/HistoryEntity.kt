package com.assistbank.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val totalAmount: Long,
    val timestamp: Long,
    val count500: Int,
    val count200: Int,
    val count100: Int,
    val count50: Int,
    val count20: Int,
    val count10: Int,
    val count5: Int,
    val count2: Int,
    val count1: Int
)
