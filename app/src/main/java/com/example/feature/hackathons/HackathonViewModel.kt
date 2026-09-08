package com.example.feature.hackathons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.HackathonEntry
import com.example.data.HackathonStatus
import com.example.data.ShinobiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HackathonViewModel(
    private val repository: ShinobiRepository
) : ViewModel() {

    // Reactive list of all hackathons sorted by nearest deadline first
    val hackathons: StateFlow<List<HackathonEntry>> = repository.allHackathons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selection for Edit/Detail BottomSheet/Dialog
    private val _selectedHackathon = MutableStateFlow<HackathonEntry?>(null)
    val selectedHackathon: StateFlow<HackathonEntry?> = _selectedHackathon.asStateFlow()

    // Sheet visibility state
    private val _isAddSheetOpen = MutableStateFlow(false)
    val isAddSheetOpen: StateFlow<Boolean> = _isAddSheetOpen.asStateFlow()

    fun openAddSheet() {
        _isAddSheetOpen.value = true
    }

    fun closeAddSheet() {
        _isAddSheetOpen.value = false
    }

    fun selectHackathon(entry: HackathonEntry?) {
        _selectedHackathon.value = entry
    }

    fun addHackathon(
        name: String,
        status: HackathonStatus,
        registrationDate: Long,
        deadlineDate: Long,
        projectLink: String? = null,
        learnings: String? = null
    ) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repository.insertHackathon(
            HackathonEntry(
                name = name.trim(),
                status = status,
                registrationDate = registrationDate,
                deadlineDate = deadlineDate,
                projectLink = projectLink?.takeIf { it.isNotBlank() }?.trim(),
                learnings = learnings?.takeIf { it.isNotBlank() }?.trim()
            )
        )
        _isAddSheetOpen.value = false
    }

    fun updateHackathon(entry: HackathonEntry) = viewModelScope.launch {
        repository.updateHackathon(entry)
        _selectedHackathon.value = null
    }

    fun deleteHackathon(entry: HackathonEntry) = viewModelScope.launch {
        repository.deleteHackathon(entry)
        _selectedHackathon.value = null
    }

    companion object {
        fun provideFactory(repository: ShinobiRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HackathonViewModel(repository) as T
                }
            }
    }
}
