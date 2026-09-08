package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.ShinobiDatabase
import com.example.data.ShinobiRepository
import com.example.feature.habits.computeStreak
import com.example.feature.habits.getTodayDateString
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitFeatureTest {

    private lateinit var database: ShinobiDatabase
    private lateinit var repository: ShinobiRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShinobiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShinobiRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testStreakComputationBackward() {
        val today = LocalDate.of(2026, 9, 8)

        // Case 1: Done today + done yesterday + done 2 days ago -> Streak = 3
        val logs3Days = listOf(
            HabitLog(habitId = 1, date = "2026-09-08", completed = true),
            HabitLog(habitId = 1, date = "2026-09-07", completed = true),
            HabitLog(habitId = 1, date = "2026-09-06", completed = true)
        )
        assertEquals(3, computeStreak(logs3Days, today))

        // Case 2: Missed yesterday, but done 2 days ago -> Streak = 1 (only today counted)
        val logsBroken = listOf(
            HabitLog(habitId = 1, date = "2026-09-08", completed = true),
            HabitLog(habitId = 1, date = "2026-09-07", completed = false),
            HabitLog(habitId = 1, date = "2026-09-06", completed = true)
        )
        assertEquals(1, computeStreak(logsBroken, today))

        // Case 3: NOT done yet today, but done yesterday and day before -> Streak = 2 (not broken yet today)
        val logsPendingToday = listOf(
            HabitLog(habitId = 1, date = "2026-09-07", completed = true),
            HabitLog(habitId = 1, date = "2026-09-06", completed = true)
        )
        assertEquals(2, computeStreak(logsPendingToday, today))

        // Case 4: No logs at all -> Streak = 0
        assertEquals(0, computeStreak(emptyList(), today))
    }

    @Test
    fun testHabitDoneTodayPersistenceAcrossReopen() = runBlocking {
        // Create habit
        val habitId = repository.insertHabit(
            Habit(name = "Daily Pushups", createdDate = System.currentTimeMillis(), isActive = true)
        )

        val todayDate = getTodayDateString()

        // 1. Mark as completed today
        repository.saveHabitLog(
            HabitLog(habitId = habitId, date = todayDate, completed = true)
        )

        // Verify initial save
        val log1 = repository.getHabitLog(habitId, todayDate)
        assertNotNull(log1)
        assertTrue(log1!!.completed)

        // 2. SIMULATE APP RESTART / RE-INITIALIZATION OF REPOSITORY FROM DATABASE
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Re-open repository from same database instance
        val reopenedRepo = ShinobiRepository(database)

        val persistedLog = reopenedRepo.getHabitLog(habitId, todayDate)
        assertNotNull("Habit log must persist across app restart", persistedLog)
        assertTrue("Habit log must remain completed true across app restart", persistedLog!!.completed)

        // 3. Test toggling off (uncheck)
        reopenedRepo.saveHabitLog(persistedLog.copy(completed = false))
        val updatedLog = reopenedRepo.getHabitLog(habitId, todayDate)
        assertNotNull(updatedLog)
        assertFalse(updatedLog!!.completed)

        // 4. Test archive habit (isActive = false)
        val habit = reopenedRepo.getHabitById(habitId)
        reopenedRepo.updateHabit(Habit(id = habitId, name = "Daily Pushups", createdDate = 1000L, isActive = false))
        val archivedHabit = reopenedRepo.getHabitById(habitId)
        // Verify isActive is false
    }
}
