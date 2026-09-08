package com.example.feature.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FocusCategory
import com.example.data.FocusSession
import com.example.data.ShinobiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.max

data class TodayFocusSummary(
    val studyMinutes: Int = 0,
    val workoutMinutes: Int = 0,
    val customMinutes: Int = 0,
    val totalMinutes: Int = 0
)

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED
}

class FocusViewModel(
    private val repository: ShinobiRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(FocusCategory.STUDY)
    val selectedCategory: StateFlow<FocusCategory> = _selectedCategory.asStateFlow()

    private val _customNote = MutableStateFlow("")
    val customNote: StateFlow<String> = _customNote.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    // Real-time summary of today's focus sessions
    val todaySummary: StateFlow<TodayFocusSummary> = repository.allFocusSessions
        .combine(_timerState) { sessions, _ ->
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val todaySessions = sessions.filter { it.timestamp in startOfDay until endOfDay }

            var study = 0
            var workout = 0
            var custom = 0

            todaySessions.forEach { session ->
                when (session.category) {
                    FocusCategory.STUDY -> study += session.durationMinutes
                    FocusCategory.WORKOUT -> workout += session.durationMinutes
                    FocusCategory.CUSTOM -> custom += session.durationMinutes
                }
            }

            TodayFocusSummary(
                studyMinutes = study,
                workoutMinutes = workout,
                customMinutes = custom,
                totalMinutes = study + workout + custom
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TodayFocusSummary()
        )

    fun selectCategory(category: FocusCategory) {
        _selectedCategory.value = category
    }

    fun updateCustomNote(note: String) {
        _customNote.value = note
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        _timerState.value = TimerState.RUNNING

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value == TimerState.RUNNING) {
                delay(1000L)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.RUNNING) return
        _timerState.value = TimerState.PAUSED
        timerJob?.cancel()
    }

    fun stopAndSaveTimer() = viewModelScope.launch {
        timerJob?.cancel()

        val seconds = _elapsedSeconds.value
        // If at least 1 second has elapsed, save it as at least 1 minute (or ceil(seconds/60))
        // So quick tests or real sessions count accurately toward focus minutes
        if (seconds > 0) {
            val minutes = max(1, ceil(seconds.toDouble() / 60.0).toInt())
            val category = _selectedCategory.value
            val note = if (category == FocusCategory.CUSTOM) {
                _customNote.value.takeIf { it.isNotBlank() }?.trim()
            } else null

            repository.insertFocusSession(
                FocusSession(
                    category = category,
                    durationMinutes = minutes,
                    timestamp = System.currentTimeMillis(),
                    note = note
                )
            )
        }

        // Reset state
        _elapsedSeconds.value = 0L
        _timerState.value = TimerState.IDLE
        _customNote.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(repository: ShinobiRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FocusViewModel(repository) as T
                }
            }
    }
}
