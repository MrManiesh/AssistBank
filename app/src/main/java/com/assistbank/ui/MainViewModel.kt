package com.assistbank.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assistbank.data.HistoryDao
import com.assistbank.data.HistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorState(
    val count500: String = "",
    val count200: String = "",
    val count100: String = "",
    val count50: String = "",
    val count20: String = "",
    val count10: String = "",
    val count5: String = "",
    val count2: String = "",
    val count1: String = ""
) {
    val totalAmount: Long
        get() = (count500.toLongOrNull() ?: 0L) * 500 +
                (count200.toLongOrNull() ?: 0L) * 200 +
                (count100.toLongOrNull() ?: 0L) * 100 +
                (count50.toLongOrNull() ?: 0L) * 50 +
                (count20.toLongOrNull() ?: 0L) * 20 +
                (count10.toLongOrNull() ?: 0L) * 10 +
                (count5.toLongOrNull() ?: 0L) * 5 +
                (count2.toLongOrNull() ?: 0L) * 2 +
                (count1.toLongOrNull() ?: 0L) * 1
}

class MainViewModel(private val historyDao: HistoryDao) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    val history: StateFlow<List<HistoryEntity>> = historyDao.getLast5History()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCount(denomination: Int, value: String) {
        // Allow empty string, but for numbers only allow digits
        if (value.isNotEmpty() && value.toLongOrNull() == null) return
        
        val currentState = _state.value
        val newState = when (denomination) {
            500 -> currentState.copy(count500 = value)
            200 -> currentState.copy(count200 = value)
            100 -> currentState.copy(count100 = value)
            50 -> currentState.copy(count50 = value)
            20 -> currentState.copy(count20 = value)
            10 -> currentState.copy(count10 = value)
            5 -> currentState.copy(count5 = value)
            2 -> currentState.copy(count2 = value)
            1 -> currentState.copy(count1 = value)
            else -> currentState
        }
        _state.value = newState
    }

    fun reset() {
        val current = _state.value
        if (current.totalAmount > 0) {
            viewModelScope.launch {
                historyDao.insertHistory(
                    HistoryEntity(
                        totalAmount = current.totalAmount,
                        timestamp = System.currentTimeMillis(),
                        count500 = current.count500.toIntOrNull() ?: 0,
                        count200 = current.count200.toIntOrNull() ?: 0,
                        count100 = current.count100.toIntOrNull() ?: 0,
                        count50 = current.count50.toIntOrNull() ?: 0,
                        count20 = current.count20.toIntOrNull() ?: 0,
                        count10 = current.count10.toIntOrNull() ?: 0,
                        count5 = current.count5.toIntOrNull() ?: 0,
                        count2 = current.count2.toIntOrNull() ?: 0,
                        count1 = current.count1.toIntOrNull() ?: 0
                    )
                )
                // cleanup in DB
                historyDao.cleanupOldHistory()
            }
        }
        _state.value = CalculatorState()
    }

    fun loadHistory(entity: HistoryEntity) {
        _state.value = CalculatorState(
            count500 = if (entity.count500 == 0) "" else entity.count500.toString(),
            count200 = if (entity.count200 == 0) "" else entity.count200.toString(),
            count100 = if (entity.count100 == 0) "" else entity.count100.toString(),
            count50 = if (entity.count50 == 0) "" else entity.count50.toString(),
            count20 = if (entity.count20 == 0) "" else entity.count20.toString(),
            count10 = if (entity.count10 == 0) "" else entity.count10.toString(),
            count5 = if (entity.count5 == 0) "" else entity.count5.toString(),
            count2 = if (entity.count2 == 0) "" else entity.count2.toString(),
            count1 = if (entity.count1 == 0) "" else entity.count1.toString()
        )
    }

    class Factory(private val historyDao: HistoryDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(historyDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
