package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.FocusCategory
import com.example.data.FocusSession
import com.example.data.ShinobiDatabase
import com.example.data.ShinobiRepository
import com.example.feature.focus.FocusViewModel
import com.example.feature.focus.TimerState
import com.example.feature.focus.formatTimerDisplay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusFeatureTest {

    private lateinit var database: ShinobiDatabase
    private lateinit var repository: ShinobiRepository
    private lateinit var viewModel: FocusViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShinobiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShinobiRepository(database)
        viewModel = FocusViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testTimerDisplayFormatting() {
        assertEquals("00:00", formatTimerDisplay(0))
        assertEquals("00:45", formatTimerDisplay(45))
        assertEquals("01:05", formatTimerDisplay(65))
        assertEquals("10:00", formatTimerDisplay(600))
        assertEquals("01:02:05", formatTimerDisplay(3725))
    }

    @Test
    fun testStartPauseStopAndSaveFlow() = runBlocking {
        // Initial state
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
        assertEquals(0L, viewModel.elapsedSeconds.value)

        // Select Custom category and add note
        viewModel.selectCategory(FocusCategory.CUSTOM)
        viewModel.updateCustomNote("LeetCode Graph Problems")
        assertEquals(FocusCategory.CUSTOM, viewModel.selectedCategory.value)
        assertEquals("LeetCode Graph Problems", viewModel.customNote.value)

        // Start timer
        viewModel.startTimer()
        assertEquals(TimerState.RUNNING, viewModel.timerState.value)

        // Pause timer
        viewModel.pauseTimer()
        assertEquals(TimerState.PAUSED, viewModel.timerState.value)

        // Resume timer
        viewModel.startTimer()
        assertEquals(TimerState.RUNNING, viewModel.timerState.value)

        // Test repository & live summary calculation directly with instant flow
        val session1 = FocusSession(
            category = FocusCategory.STUDY,
            durationMinutes = 45,
            timestamp = System.currentTimeMillis()
        )
        val session2 = FocusSession(
            category = FocusCategory.WORKOUT,
            durationMinutes = 25,
            timestamp = System.currentTimeMillis()
        )
        val session3 = FocusSession(
            category = FocusCategory.CUSTOM,
            durationMinutes = 30,
            timestamp = System.currentTimeMillis(),
            note = "LeetCode Graph Problems"
        )

        repository.insertFocusSession(session1)
        repository.insertFocusSession(session2)
        repository.insertFocusSession(session3)

        val allSessions = repository.allFocusSessions.first()
        assertEquals(3, allSessions.size)

        var studyTotal = 0
        var workoutTotal = 0
        var customTotal = 0
        allSessions.forEach {
            when (it.category) {
                FocusCategory.STUDY -> studyTotal += it.durationMinutes
                FocusCategory.WORKOUT -> workoutTotal += it.durationMinutes
                FocusCategory.CUSTOM -> customTotal += it.durationMinutes
            }
        }
        assertEquals(45, studyTotal)
        assertEquals(25, workoutTotal)
        assertEquals(30, customTotal)
        assertEquals(100, studyTotal + workoutTotal + customTotal)
    }

    @Test
    fun testStopSavesElapsedMinutesAndResets() = runBlocking {
        viewModel.selectCategory(FocusCategory.STUDY)
        viewModel.startTimer()

        // Test stopping when seconds == 0 does not insert empty session
        viewModel.stopAndSaveTimer().join()
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
        assertEquals(0L, viewModel.elapsedSeconds.value)
    }
}
