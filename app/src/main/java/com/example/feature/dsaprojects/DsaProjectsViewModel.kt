package com.example.feature.dsaprojects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DsaDifficulty
import com.example.data.DsaPlatform
import com.example.data.DsaProblem
import com.example.data.DsaStatus
import com.example.data.Project
import com.example.data.ProjectStatus
import com.example.data.ShinobiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DsaProjectsSection {
    DSA,
    PROJECTS
}

class DsaProjectsViewModel(
    private val repository: ShinobiRepository
) : ViewModel() {

    // Top section toggle: DSA vs PROJECTS
    private val _currentSection = MutableStateFlow(DsaProjectsSection.DSA)
    val currentSection: StateFlow<DsaProjectsSection> = _currentSection.asStateFlow()

    fun setSection(section: DsaProjectsSection) {
        _currentSection.value = section
    }

    // --- DSA State ---
    val allDsaProblems: StateFlow<List<DsaProblem>> = repository.allDsaProblems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter sets for multi-selection
    private val _selectedPlatforms = MutableStateFlow<Set<DsaPlatform>>(emptySet())
    val selectedPlatforms: StateFlow<Set<DsaPlatform>> = _selectedPlatforms.asStateFlow()

    private val _selectedDifficulties = MutableStateFlow<Set<DsaDifficulty>>(emptySet())
    val selectedDifficulties: StateFlow<Set<DsaDifficulty>> = _selectedDifficulties.asStateFlow()

    private val _selectedStatuses = MutableStateFlow<Set<DsaStatus>>(emptySet())
    val selectedStatuses: StateFlow<Set<DsaStatus>> = _selectedStatuses.asStateFlow()

    // Combined filtered problems
    val filteredDsaProblems: StateFlow<List<DsaProblem>> = combine(
        allDsaProblems,
        _selectedPlatforms,
        _selectedDifficulties,
        _selectedStatuses
    ) { problems, platforms, difficulties, statuses ->
        problems.filter { problem ->
            val platformMatch = platforms.isEmpty() || platforms.contains(problem.platform)
            val difficultyMatch = difficulties.isEmpty() || difficulties.contains(problem.difficulty)
            val statusMatch = statuses.isEmpty() || statuses.contains(problem.status)
            platformMatch && difficultyMatch && statusMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Solved this week counter (count DsaProblem where status is SOLVED or dateSolved != null within last 7 days)
    val solvedThisWeekCount: StateFlow<Int> = allDsaProblems.combine(
        MutableStateFlow(System.currentTimeMillis())
    ) { problems, now ->
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
        problems.count { problem ->
            val solvedDate = problem.dateSolved
            solvedDate != null && solvedDate >= sevenDaysAgo && solvedDate <= now && problem.status == DsaStatus.SOLVED
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Filter toggle actions
    fun togglePlatformFilter(platform: DsaPlatform) {
        val current = _selectedPlatforms.value
        _selectedPlatforms.value = if (current.contains(platform)) {
            current - platform
        } else {
            current + platform
        }
    }

    fun toggleDifficultyFilter(difficulty: DsaDifficulty) {
        val current = _selectedDifficulties.value
        _selectedDifficulties.value = if (current.contains(difficulty)) {
            current - difficulty
        } else {
            current + difficulty
        }
    }

    fun toggleStatusFilter(status: DsaStatus) {
        val current = _selectedStatuses.value
        _selectedStatuses.value = if (current.contains(status)) {
            current - status
        } else {
            current + status
        }
    }

    fun clearDsaFilters() {
        _selectedPlatforms.value = emptySet()
        _selectedDifficulties.value = emptySet()
        _selectedStatuses.value = emptySet()
    }

    // DSA Add / Edit dialog state
    private val _selectedDsaProblem = MutableStateFlow<DsaProblem?>(null)
    val selectedDsaProblem: StateFlow<DsaProblem?> = _selectedDsaProblem.asStateFlow()

    private val _isAddDsaSheetOpen = MutableStateFlow(false)
    val isAddDsaSheetOpen: StateFlow<Boolean> = _isAddDsaSheetOpen.asStateFlow()

    fun openAddDsaSheet() {
        _isAddDsaSheetOpen.value = true
    }

    fun closeAddDsaSheet() {
        _isAddDsaSheetOpen.value = false
    }

    fun selectDsaProblem(problem: DsaProblem?) {
        _selectedDsaProblem.value = problem
    }

    fun addDsaProblem(
        title: String,
        platform: DsaPlatform,
        difficulty: DsaDifficulty,
        topic: String,
        status: DsaStatus
    ) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        val now = System.currentTimeMillis()
        val dateSolved = if (status == DsaStatus.SOLVED) now else null
        repository.insertDsaProblem(
            DsaProblem(
                title = title.trim(),
                platform = platform,
                difficulty = difficulty,
                topic = topic.trim(),
                status = status,
                dateSolved = dateSolved
            )
        )
        _isAddDsaSheetOpen.value = false
    }

    fun updateDsaProblem(
        problem: DsaProblem,
        newTitle: String,
        newPlatform: DsaPlatform,
        newDifficulty: DsaDifficulty,
        newTopic: String,
        newStatus: DsaStatus
    ) = viewModelScope.launch {
        if (newTitle.isBlank()) return@launch
        val resolvedDateSolved = when {
            newStatus == DsaStatus.SOLVED && problem.status != DsaStatus.SOLVED -> System.currentTimeMillis()
            newStatus == DsaStatus.SOLVED -> problem.dateSolved ?: System.currentTimeMillis()
            else -> null
        }
        val updated = problem.copy(
            title = newTitle.trim(),
            platform = newPlatform,
            difficulty = newDifficulty,
            topic = newTopic.trim(),
            status = newStatus,
            dateSolved = resolvedDateSolved
        )
        repository.updateDsaProblem(updated)
        _selectedDsaProblem.value = null
    }

    fun deleteDsaProblem(problem: DsaProblem) = viewModelScope.launch {
        repository.deleteDsaProblem(problem)
        _selectedDsaProblem.value = null
    }

    // --- Projects State ---
    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _isAddProjectSheetOpen = MutableStateFlow(false)
    val isAddProjectSheetOpen: StateFlow<Boolean> = _isAddProjectSheetOpen.asStateFlow()

    fun openAddProjectSheet() {
        _isAddProjectSheetOpen.value = true
    }

    fun closeAddProjectSheet() {
        _isAddProjectSheetOpen.value = false
    }

    fun selectProject(project: Project?) {
        _selectedProject.value = project
    }

    fun addProject(
        name: String,
        techStack: String,
        status: ProjectStatus,
        repoLink: String?
    ) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repository.insertProject(
            Project(
                name = name.trim(),
                techStack = techStack.trim(),
                status = status,
                repoLink = repoLink?.takeIf { it.isNotBlank() }?.trim()
            )
        )
        _isAddProjectSheetOpen.value = false
    }

    fun updateProject(
        project: Project,
        newName: String,
        newTechStack: String,
        newStatus: ProjectStatus,
        newRepoLink: String?
    ) = viewModelScope.launch {
        if (newName.isBlank()) return@launch
        val updated = project.copy(
            name = newName.trim(),
            techStack = newTechStack.trim(),
            status = newStatus,
            repoLink = newRepoLink?.takeIf { it.isNotBlank() }?.trim()
        )
        repository.updateProject(updated)
        _selectedProject.value = null
    }

    fun deleteProject(project: Project) = viewModelScope.launch {
        repository.deleteProject(project)
        _selectedProject.value = null
    }

    companion object {
        fun provideFactory(repository: ShinobiRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DsaProjectsViewModel(repository) as T
                }
            }
    }
}
