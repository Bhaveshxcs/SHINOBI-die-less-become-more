package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DsaProblem
import com.example.data.DsaStatus
import com.example.data.FocusCategory
import com.example.data.HackathonEntry
import com.example.data.HackathonStatus
import com.example.data.ProjectStatus
import com.example.data.ShinobiRepository
import com.example.feature.habits.HabitItemUiState
import com.example.feature.habits.computeStreak
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class TodayFocusBreakdown(
    val studyMinutes: Int = 0,
    val workoutMinutes: Int = 0,
    val customMinutes: Int = 0,
    val totalMinutes: Int = 0
)

data class DsaTodaySnapshot(
    val solvedToday: Int = 0,
    val solvedThisWeek: Int = 0
)

data class ProjectsSnapshot(
    val ideaCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val totalCount: Int = 0
)

data class HomeDashboardUiState(
    val nearestHackathon: HackathonEntry? = null,
    val activeHabitsWithStreaks: List<HabitItemUiState> = emptyList(),
    val todayFocus: TodayFocusBreakdown = TodayFocusBreakdown(),
    val dsaSnapshot: DsaTodaySnapshot = DsaTodaySnapshot(),
    val projectsSnapshot: ProjectsSnapshot = ProjectsSnapshot()
)

class HomeViewModel(
    private val repository: ShinobiRepository
) : ViewModel() {

    // 1. Nearest upcoming hackathon
    // Sorted by nearest deadline >= current time (or closest if none strictly future)
    private val nearestHackathonFlow = repository.allHackathons.combine(
        repository.allHackathons // Just to combine or map
    ) { hackathons, _ ->
        val now = System.currentTimeMillis()
        // Priority: future deadlines first, sorted ascending
        val upcoming = hackathons
            .filter { it.deadlineDate >= now }
            .minByOrNull { it.deadlineDate }

        // If none strictly >= now, pick the nearest one that is not missed or the latest deadline
        upcoming ?: hackathons.minByOrNull { Math.abs(it.deadlineDate - now) }
    }

    // 2. Active habits with streaks
    private val activeHabitsFlow = combine(
        repository.activeHabits,
        repository.allHabitLogs
    ) { habits, logs ->
        val today = LocalDate.now()
        val todayStr = today.toString()
        val logsByHabit = logs.groupBy { it.habitId }

        habits.map { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            val streak = computeStreak(habitLogs, today)
            val todayLog = habitLogs.firstOrNull { it.date == todayStr }
            val isDoneToday = todayLog?.completed == true

            HabitItemUiState(
                habit = habit,
                isDoneToday = isDoneToday,
                currentStreak = streak,
                todayLog = todayLog
            )
        }
    }

    // 3. Today's focus breakdown
    private val todayFocusFlow = repository.allFocusSessions.combine(
        repository.allFocusSessions
    ) { sessions, _ ->
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

        TodayFocusBreakdown(
            studyMinutes = study,
            workoutMinutes = workout,
            customMinutes = custom,
            totalMinutes = study + workout + custom
        )
    }

    // 4. DSA problems solved today + solved this week
    private val dsaSnapshotFlow = repository.allDsaProblems.combine(
        repository.allDsaProblems
    ) { problems, _ ->
        val now = System.currentTimeMillis()
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000L)

        var solvedToday = 0
        var solvedThisWeek = 0

        problems.forEach { problem ->
            val dateSolved = problem.dateSolved
            if (problem.status == DsaStatus.SOLVED && dateSolved != null) {
                if (dateSolved in startOfDay until endOfDay) {
                    solvedToday++
                }
                if (dateSolved in sevenDaysAgo..now) {
                    solvedThisWeek++
                }
            }
        }

        DsaTodaySnapshot(
            solvedToday = solvedToday,
            solvedThisWeek = solvedThisWeek
        )
    }

    // 5. Projects snapshot
    private val projectsSnapshotFlow = repository.allProjects.combine(
        repository.allProjects
    ) { projects, _ ->
        var idea = 0
        var inProgress = 0
        var completed = 0

        projects.forEach { project ->
            when (project.status) {
                ProjectStatus.IDEA -> idea++
                ProjectStatus.IN_PROGRESS -> inProgress++
                ProjectStatus.COMPLETED -> completed++
            }
        }

        ProjectsSnapshot(
            ideaCount = idea,
            inProgressCount = inProgress,
            completedCount = completed,
            totalCount = projects.size
        )
    }

    // Combined Dashboard UI State
    val uiState: StateFlow<HomeDashboardUiState> = combine(
        nearestHackathonFlow,
        activeHabitsFlow,
        todayFocusFlow,
        dsaSnapshotFlow,
        projectsSnapshotFlow
    ) { nearest, habits, focus, dsa, projects ->
        HomeDashboardUiState(
            nearestHackathon = nearest,
            activeHabitsWithStreaks = habits,
            todayFocus = focus,
            dsaSnapshot = dsa,
            projectsSnapshot = projects
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeDashboardUiState()
    )

    companion object {
        fun provideFactory(repository: ShinobiRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
    }
}
