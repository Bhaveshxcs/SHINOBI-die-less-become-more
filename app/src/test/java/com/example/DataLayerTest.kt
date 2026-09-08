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
class DataLayerTest {

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
    fun testHackathonCrudAndSorting() = runBlocking {
        val hackathon1 = HackathonEntry(
            name = "Hack AI 2026",
            status = HackathonStatus.REGISTERED,
            registrationDate = 1000L,
            deadlineDate = 5000L,
            projectLink = "https://github.com/test/hack-ai",
            learnings = "Gemini integration"
        )
        val hackathon2 = HackathonEntry(
            name = "Mobile Hackathon",
            status = HackathonStatus.IN_PROGRESS,
            registrationDate = 1000L,
            deadlineDate = 2000L
        )

        val id1 = repository.insertHackathon(hackathon1)
        val id2 = repository.insertHackathon(hackathon2)

        assertTrue(id1 > 0)
        assertTrue(id2 > 0)

        val list = repository.allHackathons.first()
        assertEquals(2, list.size)
        // Should be sorted by deadlineDate ascending: hackathon2 (2000L) before hackathon1 (5000L)
        assertEquals("Mobile Hackathon", list[0].name)
        assertEquals("Hack AI 2026", list[1].name)

        // Test warning query within 3 days (e.g., between 1500L and 3000L)
        val warningList = repository.getUpcomingWarningHackathons(1500L, 3000L).first()
        assertEquals(1, warningList.size)
        assertEquals("Mobile Hackathon", warningList[0].name)
    }

    @Test
    fun testHabitAndHabitLogs() = runBlocking {
        val habit = Habit(name = "Daily DSA", createdDate = 1000L)
        val habitId = repository.insertHabit(habit)

        val logToday = HabitLog(habitId = habitId, date = "2026-09-08", completed = true)
        val logYesterday = HabitLog(habitId = habitId, date = "2026-09-07", completed = true)

        repository.saveHabitLog(logToday)
        repository.saveHabitLog(logYesterday)

        val logs = repository.getLogsListForHabit(habitId)
        assertEquals(2, logs.size)

        val retrievedLog = repository.getHabitLog(habitId, "2026-09-08")
        assertNotNull(retrievedLog)
        assertTrue(retrievedLog!!.completed)
    }

    @Test
    fun testFocusSessionAndDsaAndProject() = runBlocking {
        // Focus
        val session = FocusSession(
            category = FocusCategory.STUDY,
            durationMinutes = 45,
            timestamp = 1000L,
            note = "Tree algorithms"
        )
        val sessionId = repository.insertFocusSession(session)
        assertTrue(sessionId > 0)
        val totalMinutes = repository.getTotalFocusMinutesBetween(500L, 1500L).first()
        assertEquals(45, totalMinutes)

        // DSA
        val dsa = DsaProblem(
            title = "Two Sum",
            platform = DsaPlatform.LEETCODE,
            difficulty = DsaDifficulty.EASY,
            topic = "Arrays & Hashing",
            status = DsaStatus.SOLVED,
            dateSolved = 1200L
        )
        val dsaId = repository.insertDsaProblem(dsa)
        assertTrue(dsaId > 0)
        val solvedCount = repository.solvedDsaCount.first()
        assertEquals(1, solvedCount)

        // Project
        val project = Project(
            name = "Shinobi App",
            techStack = "Kotlin, Jetpack Compose, Room",
            status = ProjectStatus.IN_PROGRESS,
            repoLink = "https://github.com/user/shinobi"
        )
        val projId = repository.insertProject(project)
        assertTrue(projId > 0)
        val projectList = repository.allProjects.first()
        assertEquals(1, projectList.size)
        assertEquals("Shinobi App", projectList[0].name)
    }
}
