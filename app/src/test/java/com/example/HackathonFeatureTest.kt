package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.HackathonEntry
import com.example.data.HackathonStatus
import com.example.data.ShinobiDatabase
import com.example.data.ShinobiRepository
import com.example.feature.hackathons.HackathonViewModel
import com.example.feature.hackathons.getDaysRemaining
import com.example.feature.hackathons.getStatusColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HackathonFeatureTest {

    private lateinit var database: ShinobiDatabase
    private lateinit var repository: ShinobiRepository
    private lateinit var viewModel: HackathonViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShinobiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShinobiRepository(database)
        viewModel = HackathonViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAddEditDeleteFlow() = runBlocking {
        val now = System.currentTimeMillis()
        val inTwoDays = now + (2L * 86400000L)

        // 1. Add Hackathon
        viewModel.addHackathon(
            name = "Shinobi CodeSprint",
            status = HackathonStatus.IN_PROGRESS,
            registrationDate = now,
            deadlineDate = inTwoDays,
            projectLink = "https://github.com/test/sprint",
            learnings = "Prototyped Compose M3 screens"
        ).join()

        val list = repository.allHackathons.first()
        assertEquals(1, list.size)
        val item = list[0]
        assertEquals("Shinobi CodeSprint", item.name)
        assertEquals(HackathonStatus.IN_PROGRESS, item.status)
        assertEquals("https://github.com/test/sprint", item.projectLink)

        // 2. Select and Edit
        viewModel.selectHackathon(item)
        assertEquals(item, viewModel.selectedHackathon.value)

        val updated = item.copy(
            status = HackathonStatus.SUBMITTED,
            learnings = "Submitted on Devpost with demo video!"
        )
        viewModel.updateHackathon(updated).join()

        val updatedList = repository.allHackathons.first()
        assertEquals(1, updatedList.size)
        assertEquals(HackathonStatus.SUBMITTED, updatedList[0].status)
        assertEquals("Submitted on Devpost with demo video!", updatedList[0].learnings)
        assertNull(viewModel.selectedHackathon.value)

        // 3. Delete
        viewModel.deleteHackathon(updatedList[0]).join()
        val emptyList = repository.allHackathons.first()
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun testWarningCardCriteria() {
        val now = System.currentTimeMillis()
        val inOneDay = now + 86400000L
        val inFourDays = now + (4L * 86400000L)

        val urgentUnsubmitted = HackathonEntry(
            id = 1,
            name = "Urgent Hack",
            status = HackathonStatus.IN_PROGRESS,
            registrationDate = now,
            deadlineDate = inOneDay
        )

        val submittedEntry = HackathonEntry(
            id = 2,
            name = "Done Hack",
            status = HackathonStatus.SUBMITTED,
            registrationDate = now,
            deadlineDate = inOneDay
        )

        val farAwayEntry = HackathonEntry(
            id = 3,
            name = "Future Hack",
            status = HackathonStatus.REGISTERED,
            registrationDate = now,
            deadlineDate = inFourDays
        )

        val threeDaysMillis = 3L * 86400000L

        // Urgent Hack: not submitted AND deadline <= 3 days away -> Warning should trigger!
        assertTrue(
            urgentUnsubmitted.status != HackathonStatus.SUBMITTED &&
            (urgentUnsubmitted.deadlineDate - now) <= threeDaysMillis
        )

        // Submitted Hack: status is SUBMITTED -> Warning should NOT trigger
        assertFalse(
            submittedEntry.status != HackathonStatus.SUBMITTED &&
            (submittedEntry.deadlineDate - now) <= threeDaysMillis
        )

        // Far away Hack: > 3 days -> Warning should NOT trigger
        assertFalse(
            farAwayEntry.status != HackathonStatus.SUBMITTED &&
            (farAwayEntry.deadlineDate - now) <= threeDaysMillis
        )
    }

    @Test
    fun testStatusColors() {
        assertNotNull(getStatusColor(HackathonStatus.REGISTERED))
        assertNotNull(getStatusColor(HackathonStatus.IN_PROGRESS))
        assertNotNull(getStatusColor(HackathonStatus.SUBMITTED))
        assertNotNull(getStatusColor(HackathonStatus.MISSED))
    }
}
