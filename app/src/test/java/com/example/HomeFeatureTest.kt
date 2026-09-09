package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.DsaDifficulty
import com.example.data.DsaPlatform
import com.example.data.DsaProblem
import com.example.data.DsaStatus
import com.example.data.FocusCategory
import com.example.data.FocusSession
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.HackathonEntry
import com.example.data.HackathonStatus
import com.example.data.Project
import com.example.data.ProjectStatus
import com.example.data.ShinobiDatabase
import com.example.data.ShinobiRepository
import com.example.feature.home.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeFeatureTest {

    private lateinit var database: ShinobiDatabase
    private lateinit var repository: ShinobiRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShinobiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShinobiRepository(database)
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testEmptyDashboardState() = runBlocking {
        val state = viewModel.uiState.first()
        assertNull(state.nearestHackathon)
        assertEquals(0, state.activeHabitsWithStreaks.size)
        assertEquals(0, state.todayFocus.totalMinutes)
        assertEquals(0, state.dsaSnapshot.solvedToday)
        assertEquals(0, state.dsaSnapshot.solvedThisWeek)
        assertEquals(0, state.projectsSnapshot.totalCount)
    }

    @Test
    fun testLiveAggregationAcrossModules() = runBlocking {
        val now = System.currentTimeMillis()
        val todayStr = LocalDate.now().toString()

        // 1. Insert Hackathons: one far, one near
        repository.insertHackathon(
            HackathonEntry(
                name = "Far Hackathon",
                status = HackathonStatus.REGISTERED,
                registrationDate = now,
                deadlineDate = now + (10L * 86400000L)
            )
        )
        val nearId = repository.insertHackathon(
            HackathonEntry(
                name = "Near Hackathon",
                status = HackathonStatus.IN_PROGRESS,
                registrationDate = now,
                deadlineDate = now + (2L * 86400000L)
            )
        )

        // 2. Insert Habit & logs
        val habitId = repository.insertHabit(
            Habit(name = "LeetCode Daily", createdDate = now, isActive = true)
        )
        repository.saveHabitLog(
            HabitLog(habitId = habitId, date = todayStr, completed = true)
        )

        // 3. Insert Focus Sessions
        repository.insertFocusSession(
            FocusSession(category = FocusCategory.STUDY, durationMinutes = 45, timestamp = now)
        )
        repository.insertFocusSession(
            FocusSession(category = FocusCategory.WORKOUT, durationMinutes = 30, timestamp = now)
        )

        // 4. Insert DSA Problems
        repository.insertDsaProblem(
            DsaProblem(
                title = "Valid Palindrome",
                platform = DsaPlatform.LEETCODE,
                difficulty = DsaDifficulty.EASY,
                topic = "Strings",
                status = DsaStatus.SOLVED,
                dateSolved = now
            )
        )

        // 5. Insert Projects
        repository.insertProject(
            Project(name = "Shinobi App", techStack = "Compose", status = ProjectStatus.IN_PROGRESS)
        )
        repository.insertProject(
            Project(name = "AI Chatbot", techStack = "Python", status = ProjectStatus.IDEA)
        )

        // Verify Live Dashboard
        val state = viewModel.uiState.first()

        // Nearest Hackathon should be Near Hackathon
        assertNotNull(state.nearestHackathon)
        assertEquals("Near Hackathon", state.nearestHackathon?.name)

        // Habit streak should be 1
        assertEquals(1, state.activeHabitsWithStreaks.size)
        assertEquals(1, state.activeHabitsWithStreaks[0].currentStreak)
        assertEquals("LeetCode Daily", state.activeHabitsWithStreaks[0].habit.name)

        // Focus breakdown
        assertEquals(45, state.todayFocus.studyMinutes)
        assertEquals(30, state.todayFocus.workoutMinutes)
        assertEquals(75, state.todayFocus.totalMinutes)

        // DSA snapshot
        assertEquals(1, state.dsaSnapshot.solvedToday)
        assertEquals(1, state.dsaSnapshot.solvedThisWeek)

        // Projects snapshot
        assertEquals(1, state.projectsSnapshot.inProgressCount)
        assertEquals(1, state.projectsSnapshot.ideaCount)
        assertEquals(0, state.projectsSnapshot.completedCount)
        assertEquals(2, state.projectsSnapshot.totalCount)
    }
}
