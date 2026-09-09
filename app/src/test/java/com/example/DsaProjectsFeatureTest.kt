package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.DsaDifficulty
import com.example.data.DsaPlatform
import com.example.data.DsaProblem
import com.example.data.DsaStatus
import com.example.data.Project
import com.example.data.ProjectStatus
import com.example.data.ShinobiDatabase
import com.example.data.ShinobiRepository
import com.example.feature.dsaprojects.DsaProjectsSection
import com.example.feature.dsaprojects.DsaProjectsViewModel
import com.example.feature.dsaprojects.getDsaDifficultyColor
import com.example.feature.dsaprojects.getDsaStatusColor
import com.example.feature.dsaprojects.getProjectStatusColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
class DsaProjectsFeatureTest {

    private lateinit var database: ShinobiDatabase
    private lateinit var repository: ShinobiRepository
    private lateinit var viewModel: DsaProjectsViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShinobiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShinobiRepository(database)
        viewModel = DsaProjectsViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSectionToggle() {
        assertEquals(DsaProjectsSection.DSA, viewModel.currentSection.value)
        viewModel.setSection(DsaProjectsSection.PROJECTS)
        assertEquals(DsaProjectsSection.PROJECTS, viewModel.currentSection.value)
        viewModel.setSection(DsaProjectsSection.DSA)
        assertEquals(DsaProjectsSection.DSA, viewModel.currentSection.value)
    }

    @Test
    fun testDsaProblemAddEditDeleteAndFiltering() = runBlocking {
        // 1. Add 3 problems
        viewModel.addDsaProblem(
            title = "Two Sum",
            platform = DsaPlatform.LEETCODE,
            difficulty = DsaDifficulty.EASY,
            topic = "Array & Hash Table",
            status = DsaStatus.SOLVED
        ).join()

        viewModel.addDsaProblem(
            title = "LRU Cache",
            platform = DsaPlatform.LEETCODE,
            difficulty = DsaDifficulty.MEDIUM,
            topic = "Design",
            status = DsaStatus.ATTEMPTED
        ).join()

        viewModel.addDsaProblem(
            title = "Codeforces Round 900 Div 2 C",
            platform = DsaPlatform.CODEFORCES,
            difficulty = DsaDifficulty.HARD,
            topic = "Math",
            status = DsaStatus.TO_DO
        ).join()

        val all = repository.allDsaProblems.first()
        assertEquals(3, all.size)

        // Solved problem should have a non-null dateSolved
        val twoSum = all.first { it.title == "Two Sum" }
        assertNotNull(twoSum.dateSolved)
        assertEquals(DsaStatus.SOLVED, twoSum.status)

        // 2. Test Multi-filter: Platform LEETCODE
        viewModel.togglePlatformFilter(DsaPlatform.LEETCODE)
        var filtered = viewModel.filteredDsaProblems.first()
        assertEquals(2, filtered.size)

        // Add Difficulty EASY
        viewModel.toggleDifficultyFilter(DsaDifficulty.EASY)
        filtered = viewModel.filteredDsaProblems.first()
        assertEquals(1, filtered.size)
        assertEquals("Two Sum", filtered[0].title)

        // Clear filters
        viewModel.clearDsaFilters()
        filtered = viewModel.filteredDsaProblems.first()
        assertEquals(3, filtered.size)

        // 3. Edit problem: Update LRU Cache to SOLVED
        val lru = all.first { it.title == "LRU Cache" }
        viewModel.selectDsaProblem(lru)
        assertEquals(lru, viewModel.selectedDsaProblem.value)

        viewModel.updateDsaProblem(
            problem = lru,
            newTitle = "LRU Cache (Double Linked List)",
            newPlatform = DsaPlatform.LEETCODE,
            newDifficulty = DsaDifficulty.MEDIUM,
            newTopic = "Design & DLL",
            newStatus = DsaStatus.SOLVED
        ).join()

        val updatedAll = repository.allDsaProblems.first()
        val updatedLru = updatedAll.first { it.id == lru.id }
        assertEquals("LRU Cache (Double Linked List)", updatedLru.title)
        assertEquals(DsaStatus.SOLVED, updatedLru.status)
        assertNotNull(updatedLru.dateSolved)
        assertNull(viewModel.selectedDsaProblem.value)

        // 4. Solved This Week counter
        val solvedWeek = viewModel.solvedThisWeekCount.first()
        assertEquals(2, solvedWeek) // Two Sum and LRU Cache

        // 5. Delete problem
        viewModel.deleteDsaProblem(updatedLru).join()
        val afterDelete = repository.allDsaProblems.first()
        assertEquals(2, afterDelete.size)
    }

    @Test
    fun testProjectAddEditDeleteFlow() = runBlocking {
        // 1. Add project
        viewModel.addProject(
            name = "Shinobi Android",
            techStack = "Kotlin, Jetpack Compose, Room M3",
            status = ProjectStatus.IN_PROGRESS,
            repoLink = "https://github.com/developer/shinobi"
        ).join()

        viewModel.addProject(
            name = "Local LLM Runner",
            techStack = "C++, ONNX Runtime, Python",
            status = ProjectStatus.IDEA,
            repoLink = null
        ).join()

        val allProjects = repository.allProjects.first()
        assertEquals(2, allProjects.size)

        val shinobiProj = allProjects.first { it.name == "Shinobi Android" }
        assertEquals(ProjectStatus.IN_PROGRESS, shinobiProj.status)
        assertEquals("https://github.com/developer/shinobi", shinobiProj.repoLink)

        // 2. Select and Edit
        viewModel.selectProject(shinobiProj)
        assertEquals(shinobiProj, viewModel.selectedProject.value)

        viewModel.updateProject(
            project = shinobiProj,
            newName = "Shinobi Android Master",
            newTechStack = "Kotlin 2.0, Compose M3, Room, Robolectric",
            newStatus = ProjectStatus.COMPLETED,
            newRepoLink = "https://github.com/developer/shinobi-master"
        ).join()

        val updatedProjects = repository.allProjects.first()
        val updatedShinobi = updatedProjects.first { it.id == shinobiProj.id }
        assertEquals("Shinobi Android Master", updatedShinobi.name)
        assertEquals(ProjectStatus.COMPLETED, updatedShinobi.status)
        assertEquals("https://github.com/developer/shinobi-master", updatedShinobi.repoLink)
        assertNull(viewModel.selectedProject.value)

        // 3. Delete Project
        viewModel.deleteProject(updatedShinobi).join()
        val remaining = repository.allProjects.first()
        assertEquals(1, remaining.size)
        assertEquals("Local LLM Runner", remaining[0].name)
    }

    @Test
    fun testStatusAndDifficultyColorHelpers() {
        assertNotNull(getDsaDifficultyColor(DsaDifficulty.EASY))
        assertNotNull(getDsaDifficultyColor(DsaDifficulty.MEDIUM))
        assertNotNull(getDsaDifficultyColor(DsaDifficulty.HARD))

        assertNotNull(getProjectStatusColor(ProjectStatus.IDEA))
        assertNotNull(getProjectStatusColor(ProjectStatus.IN_PROGRESS))
        assertNotNull(getProjectStatusColor(ProjectStatus.COMPLETED))

        assertNotNull(getDsaStatusColor(DsaStatus.SOLVED))
        assertNotNull(getDsaStatusColor(DsaStatus.ATTEMPTED))
        assertNotNull(getDsaStatusColor(DsaStatus.TO_DO))
    }
}
