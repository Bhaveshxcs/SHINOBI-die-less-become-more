package com.example.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.ShinobiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(
    private val repository: ShinobiRepository
) : ViewModel() {

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    // Combine active (or all) habits and habit logs to produce real-time UI states with computed streaks
    val habitItems: StateFlow<List<HabitItemUiState>> = combine(
        repository.allHabits,
        repository.allHabitLogs,
        _showArchived
    ) { habits, logs, showArchivedFlag ->
        val filteredHabits = if (showArchivedFlag) habits else habits.filter { it.isActive }
        val logsByHabit = logs.groupBy { it.habitId }
        val todayStr = getTodayDateString()

        filteredHabits.map { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            val todayLog = habitLogs.firstOrNull { it.date == todayStr }
            val isDoneToday = todayLog?.completed == true
            val streak = computeStreak(habitLogs)

            HabitItemUiState(
                habit = habit,
                isDoneToday = isDoneToday,
                currentStreak = streak,
                todayLog = todayLog
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun toggleShowArchived() {
        _showArchived.value = !_showArchived.value
    }

    fun addHabit(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repository.insertHabit(
            Habit(
                name = name.trim(),
                createdDate = System.currentTimeMillis(),
                isActive = true
            )
        )
        _isAddDialogOpen.value = false
    }

    fun toggleHabitDoneToday(item: HabitItemUiState) = viewModelScope.launch {
        val todayStr = getTodayDateString()
        val newCompleted = !item.isDoneToday

        val logToSave = item.todayLog?.copy(completed = newCompleted)
            ?: HabitLog(
                habitId = item.habit.id,
                date = todayStr,
                completed = newCompleted
            )
        repository.saveHabitLog(logToSave)
    }

    fun archiveHabit(habit: Habit) = viewModelScope.launch {
        repository.updateHabit(habit.copy(isActive = false))
    }

    fun unarchiveHabit(habit: Habit) = viewModelScope.launch {
        repository.updateHabit(habit.copy(isActive = true))
    }

    fun deleteHabitPermanently(habit: Habit) = viewModelScope.launch {
        repository.deleteHabit(habit)
    }

    companion object {
        fun provideFactory(repository: ShinobiRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HabitViewModel(repository) as T
                }
            }
    }
}
