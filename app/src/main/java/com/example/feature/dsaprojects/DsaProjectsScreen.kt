package com.example.feature.dsaprojects

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DsaDifficulty
import com.example.data.DsaPlatform
import com.example.data.DsaProblem
import com.example.data.DsaStatus
import com.example.data.Project
import com.example.data.ProjectStatus
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MysticIndigo
import com.example.ui.theme.ShinobiRed
import com.example.ui.theme.SkyTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

// Helper colors for DsaDifficulty: EASY=green, MEDIUM=amber, HARD=red
fun getDsaDifficultyColor(difficulty: DsaDifficulty): Color = when (difficulty) {
    DsaDifficulty.EASY -> MintGreen
    DsaDifficulty.MEDIUM -> AmberGold
    DsaDifficulty.HARD -> ShinobiRed
}

// Helper colors for ProjectStatus
fun getProjectStatusColor(status: ProjectStatus): Color = when (status) {
    ProjectStatus.IDEA -> SkyTeal
    ProjectStatus.IN_PROGRESS -> AmberGold
    ProjectStatus.COMPLETED -> MintGreen
}

// Helper colors for DsaStatus
fun getDsaStatusColor(status: DsaStatus): Color = when (status) {
    DsaStatus.SOLVED -> MintGreen
    DsaStatus.ATTEMPTED -> AmberGold
    DsaStatus.TO_DO -> TextSecondary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DsaProjectsScreen(
    viewModel: DsaProjectsViewModel,
    modifier: Modifier = Modifier
) {
    val currentSection by viewModel.currentSection.collectAsState()
    val isAddDsaOpen by viewModel.isAddDsaSheetOpen.collectAsState()
    val selectedDsaProblem by viewModel.selectedDsaProblem.collectAsState()
    val isAddProjectOpen by viewModel.isAddProjectSheetOpen.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("dsa_projects_screen"),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentSection == DsaProjectsSection.DSA) {
                        viewModel.openAddDsaSheet()
                    } else {
                        viewModel.openAddProjectSheet()
                    }
                },
                containerColor = ShinobiRed,
                contentColor = Color.White,
                modifier = Modifier.testTag(
                    if (currentSection == DsaProjectsSection.DSA) "fab_add_dsa_problem" else "fab_add_project"
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (currentSection == DsaProjectsSection.DSA) "Add DSA Problem" else "Add Project"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DSA & Projects",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.testTag("dsa_projects_header_title")
                    )
                    Text(
                        text = "Sharpen problem-solving and software mastery",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Top Section Toggle: DSA vs Projects
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("section_toggle_row")
            ) {
                SegmentedButton(
                    selected = currentSection == DsaProjectsSection.DSA,
                    onClick = { viewModel.setSection(DsaProjectsSection.DSA) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = DarkSurfaceVariant,
                        activeContentColor = ShinobiRed,
                        inactiveContainerColor = DarkSurface,
                        inactiveContentColor = TextSecondary,
                        activeBorderColor = ShinobiRed,
                        inactiveBorderColor = BorderColor
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("toggle_dsa_section")
                ) {
                    Text(
                        text = "DSA",
                        fontWeight = if (currentSection == DsaProjectsSection.DSA) FontWeight.Bold else FontWeight.Normal
                    )
                }

                SegmentedButton(
                    selected = currentSection == DsaProjectsSection.PROJECTS,
                    onClick = { viewModel.setSection(DsaProjectsSection.PROJECTS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = DarkSurfaceVariant,
                        activeContentColor = SkyTeal,
                        inactiveContainerColor = DarkSurface,
                        inactiveContentColor = TextSecondary,
                        activeBorderColor = SkyTeal,
                        inactiveBorderColor = BorderColor
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("toggle_projects_section")
                ) {
                    Text(
                        text = "Projects",
                        fontWeight = if (currentSection == DsaProjectsSection.PROJECTS) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Content according to selected section
            when (currentSection) {
                DsaProjectsSection.DSA -> {
                    DsaSectionContent(viewModel = viewModel)
                }
                DsaProjectsSection.PROJECTS -> {
                    ProjectsSectionContent(viewModel = viewModel)
                }
            }
        }

        // Modals / BottomSheets
        if (isAddDsaOpen) {
            AddDsaProblemBottomSheet(
                onDismiss = { viewModel.closeAddDsaSheet() },
                onAdd = { title, platform, difficulty, topic, status ->
                    viewModel.addDsaProblem(title, platform, difficulty, topic, status)
                }
            )
        }

        selectedDsaProblem?.let { problem ->
            EditDsaProblemBottomSheet(
                problem = problem,
                onDismiss = { viewModel.selectDsaProblem(null) },
                onUpdate = { t, p, d, top, s ->
                    viewModel.updateDsaProblem(problem, t, p, d, top, s)
                },
                onDelete = {
                    viewModel.deleteDsaProblem(problem)
                }
            )
        }

        if (isAddProjectOpen) {
            AddProjectBottomSheet(
                onDismiss = { viewModel.closeAddProjectSheet() },
                onAdd = { name, techStack, status, repoLink ->
                    viewModel.addProject(name, techStack, status, repoLink)
                }
            )
        }

        selectedProject?.let { project ->
            EditProjectBottomSheet(
                project = project,
                onDismiss = { viewModel.selectProject(null) },
                onUpdate = { n, t, s, r ->
                    viewModel.updateProject(project, n, t, s, r)
                },
                onDelete = {
                    viewModel.deleteProject(project)
                }
            )
        }
    }
}

// -------------------------------------------------------------
// DSA Section Content
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DsaSectionContent(
    viewModel: DsaProjectsViewModel
) {
    val filteredProblems by viewModel.filteredDsaProblems.collectAsState()
    val totalSolvedWeek by viewModel.solvedThisWeekCount.collectAsState()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsState()
    val selectedDifficulties by viewModel.selectedDifficulties.collectAsState()
    val selectedStatuses by viewModel.selectedStatuses.collectAsState()

    val totalActiveFilters = selectedPlatforms.size + selectedDifficulties.size + selectedStatuses.size

    Column(modifier = Modifier.fillMaxSize()) {
        // Counter card at the top: Total Solved This Week
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("dsa_solved_week_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MintGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Solved count",
                            tint = MintGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Solved This Week",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Within the last 7 days",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                Text(
                    text = "$totalSolvedWeek",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MintGreen
                    ),
                    modifier = Modifier.testTag("dsa_solved_week_count")
                )
            }
        }

        // Filter Header & Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filters" + if (totalActiveFilters > 0) " ($totalActiveFilters active)" else "",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (totalActiveFilters > 0) {
                TextButton(
                    onClick = { viewModel.clearDsaFilters() },
                    modifier = Modifier.testTag("clear_dsa_filters_button")
                ) {
                    Text("Clear All", color = ShinobiRed, fontSize = 12.sp)
                }
            }
        }

        // Horizontally scrolling filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
                .testTag("dsa_filters_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Platform chips
            DsaPlatform.values().forEach { platform ->
                val isSelected = selectedPlatforms.contains(platform)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.togglePlatformFilter(platform) },
                    label = { Text(platform.name) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurface,
                        labelColor = TextSecondary,
                        selectedContainerColor = SkyTeal.copy(alpha = 0.2f),
                        selectedLabelColor = SkyTeal,
                        selectedLeadingIconColor = SkyTeal
                    ),
                    border = BorderStroke(1.dp, if (isSelected) SkyTeal else BorderColor),
                    modifier = Modifier.testTag("filter_chip_platform_${platform.name.lowercase()}")
                )
            }

            // Difficulty chips (Color coded)
            DsaDifficulty.values().forEach { difficulty ->
                val isSelected = selectedDifficulties.contains(difficulty)
                val diffColor = getDsaDifficultyColor(difficulty)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleDifficultyFilter(difficulty) },
                    label = { Text(difficulty.name) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurface,
                        labelColor = diffColor,
                        selectedContainerColor = diffColor.copy(alpha = 0.2f),
                        selectedLabelColor = diffColor,
                        selectedLeadingIconColor = diffColor
                    ),
                    border = BorderStroke(1.dp, if (isSelected) diffColor else BorderColor),
                    modifier = Modifier.testTag("filter_chip_difficulty_${difficulty.name.lowercase()}")
                )
            }

            // Status chips
            DsaStatus.values().forEach { status ->
                val isSelected = selectedStatuses.contains(status)
                val statusColor = getDsaStatusColor(status)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleStatusFilter(status) },
                    label = { Text(status.name.replace('_', ' ')) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurface,
                        labelColor = TextSecondary,
                        selectedContainerColor = statusColor.copy(alpha = 0.2f),
                        selectedLabelColor = statusColor,
                        selectedLeadingIconColor = statusColor
                    ),
                    border = BorderStroke(1.dp, if (isSelected) statusColor else BorderColor),
                    modifier = Modifier.testTag("filter_chip_status_${status.name.lowercase()}")
                )
            }
        }

        // Problems List
        if (filteredProblems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dsa_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (totalActiveFilters > 0) "No problems match current filters" else "No DSA problems logged yet",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (totalActiveFilters > 0) "Try tweaking or clearing active filter chips" else "Tap + to add your first problem",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary.copy(alpha = 0.7f))
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dsa_problems_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredProblems,
                    key = { it.id }
                ) { problem ->
                    DsaProblemItemCard(
                        problem = problem,
                        onClick = { viewModel.selectDsaProblem(problem) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DSA Problem Item Card
// -------------------------------------------------------------
@Composable
private fun DsaProblemItemCard(
    problem: DsaProblem,
    onClick: () -> Unit
) {
    val diffColor = getDsaDifficultyColor(problem.difficulty)
    val statusColor = getDsaStatusColor(problem.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("dsa_problem_item_${problem.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Title and Status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = problem.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = problem.status.name.replace('_', ' '),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges: Platform, Difficulty (Color-coded: EASY=green, MEDIUM=amber, HARD=red), Topic
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Platform badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text(
                        text = problem.platform.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SkyTeal,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Difficulty badge (EASY=green, MEDIUM=amber, HARD=red)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = diffColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, diffColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = problem.difficulty.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = diffColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Topic chip
                if (problem.topic.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = problem.topic,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Projects Section Content
// -------------------------------------------------------------
@Composable
private fun ProjectsSectionContent(
    viewModel: DsaProjectsViewModel
) {
    val projects by viewModel.allProjects.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("projects_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No projects tracked yet",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap + to track ideas, active prototypes & completed repos",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary.copy(alpha = 0.7f))
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("projects_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = projects,
                    key = { it.id }
                ) { project ->
                    ProjectItemCard(
                        project = project,
                        onClick = { viewModel.selectProject(project) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Project Item Card
// -------------------------------------------------------------
@Composable
private fun ProjectItemCard(
    project: Project,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val statusColor = getProjectStatusColor(project.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("project_item_${project.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Project Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Status badge (IDEA / IN_PROGRESS / COMPLETED)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = project.status.name.replace('_', ' '),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tech stack display
            if (project.techStack.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = project.techStack,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Repo Link if present (clickable)
            if (!project.repoLink.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.clickable {
                        try {
                            val url = if (!project.repoLink.startsWith("http://") && !project.repoLink.startsWith("https://")) {
                                "https://${project.repoLink}"
                            } else {
                                project.repoLink
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open link: ${project.repoLink}", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Repository",
                            tint = SkyTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = project.repoLink,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SkyTeal,
                                textDecoration = TextDecoration.Underline
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open repository",
                            tint = SkyTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Add DSA Problem Bottom Sheet
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDsaProblemBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (title: String, platform: DsaPlatform, difficulty: DsaDifficulty, topic: String, status: DsaStatus) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf(DsaPlatform.LEETCODE) }
    var difficulty by remember { mutableStateOf(DsaDifficulty.MEDIUM) }
    var topic by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(DsaStatus.SOLVED) }

    var platformExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        modifier = Modifier.testTag("add_dsa_problem_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add DSA Problem",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Problem Title (e.g., 3Sum, Trapping Rain Water)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dsa_input_title"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Platform Dropdown
            ExposedDropdownMenuBox(
                expanded = platformExpanded,
                onExpandedChange = { platformExpanded = !platformExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = platform.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Platform") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dsa_dropdown_platform")
                )
                ExposedDropdownMenu(
                    expanded = platformExpanded,
                    onDismissRequest = { platformExpanded = false }
                ) {
                    DsaPlatform.values().forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                platform = p
                                platformExpanded = false
                            },
                            modifier = Modifier.testTag("dsa_platform_item_${p.name.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty Dropdown
            ExposedDropdownMenuBox(
                expanded = difficultyExpanded,
                onExpandedChange = { difficultyExpanded = !difficultyExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = difficulty.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Difficulty") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dsa_dropdown_difficulty")
                )
                ExposedDropdownMenu(
                    expanded = difficultyExpanded,
                    onDismissRequest = { difficultyExpanded = false }
                ) {
                    DsaDifficulty.values().forEach { d ->
                        val color = getDsaDifficultyColor(d)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = d.name,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                difficulty = d
                                difficultyExpanded = false
                            },
                            modifier = Modifier.testTag("dsa_difficulty_item_${d.name.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Topic text field
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic / Tag (e.g., Dynamic Programming, Graphs)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dsa_input_topic"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dsa_dropdown_status")
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    DsaStatus.values().forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.name.replace('_', ' ')) },
                            onClick = {
                                status = s
                                statusExpanded = false
                            },
                            modifier = Modifier.testTag("dsa_status_item_${s.name.lowercase()}")
                        )
                    }
                }
            }

            if (status == DsaStatus.SOLVED) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Status is SOLVED — will automatically record dateSolved to today",
                    style = MaterialTheme.typography.bodySmall.copy(color = MintGreen)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, platform, difficulty, topic, status)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_add_dsa_button")
            ) {
                Text("Save Problem", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// -------------------------------------------------------------
// Edit DSA Problem Bottom Sheet
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDsaProblemBottomSheet(
    problem: DsaProblem,
    onDismiss: () -> Unit,
    onUpdate: (title: String, platform: DsaPlatform, difficulty: DsaDifficulty, topic: String, status: DsaStatus) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(problem.title) }
    var platform by remember { mutableStateOf(problem.platform) }
    var difficulty by remember { mutableStateOf(problem.difficulty) }
    var topic by remember { mutableStateOf(problem.topic) }
    var status by remember { mutableStateOf(problem.status) }

    var platformExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Problem?") },
            text = { Text("Are you sure you want to remove '${problem.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    modifier = Modifier.testTag("confirm_delete_dsa_button")
                ) {
                    Text("Delete", color = ShinobiRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        modifier = Modifier.testTag("edit_dsa_problem_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit DSA Problem",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Problem Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_dsa_input_title"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = platformExpanded,
                onExpandedChange = { platformExpanded = !platformExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = platform.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Platform") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("edit_dsa_dropdown_platform")
                )
                ExposedDropdownMenu(
                    expanded = platformExpanded,
                    onDismissRequest = { platformExpanded = false }
                ) {
                    DsaPlatform.values().forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                platform = p
                                platformExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = difficultyExpanded,
                onExpandedChange = { difficultyExpanded = !difficultyExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = difficulty.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Difficulty") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("edit_dsa_dropdown_difficulty")
                )
                ExposedDropdownMenu(
                    expanded = difficultyExpanded,
                    onDismissRequest = { difficultyExpanded = false }
                ) {
                    DsaDifficulty.values().forEach { d ->
                        val color = getDsaDifficultyColor(d)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = d.name,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                difficulty = d
                                difficultyExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic / Tag") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_dsa_input_topic"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("edit_dsa_dropdown_status")
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    DsaStatus.values().forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.name.replace('_', ' ')) },
                            onClick = {
                                status = s
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    border = BorderStroke(1.dp, ShinobiRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ShinobiRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("delete_dsa_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onUpdate(title, platform, difficulty, topic, status)
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("save_edit_dsa_button")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// -------------------------------------------------------------
// Add Project Bottom Sheet
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProjectBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, techStack: String, status: ProjectStatus, repoLink: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var techStack by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ProjectStatus.IN_PROGRESS) }
    var repoLink by remember { mutableStateOf("") }
    var statusExpanded by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        modifier = Modifier.testTag("add_project_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add Project",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_input_name"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = techStack,
                onValueChange = { techStack = it },
                label = { Text("Tech Stack (e.g., Kotlin, Compose, Room)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_input_tech_stack"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("project_dropdown_status")
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    ProjectStatus.values().forEach { s ->
                        val color = getProjectStatusColor(s)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = s.name.replace('_', ' '),
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                status = s
                                statusExpanded = false
                            },
                            modifier = Modifier.testTag("project_status_item_${s.name.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = repoLink,
                onValueChange = { repoLink = it },
                label = { Text("Repository Link (optional, e.g., github.com/user/repo)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_input_repo_link"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, techStack, status, repoLink)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_add_project_button")
            ) {
                Text("Save Project", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// -------------------------------------------------------------
// Edit Project Bottom Sheet
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProjectBottomSheet(
    project: Project,
    onDismiss: () -> Unit,
    onUpdate: (name: String, techStack: String, status: ProjectStatus, repoLink: String?) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var techStack by remember { mutableStateOf(project.techStack) }
    var status by remember { mutableStateOf(project.status) }
    var repoLink by remember { mutableStateOf(project.repoLink ?: "") }
    var statusExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Project?") },
            text = { Text("Are you sure you want to remove '${project.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    modifier = Modifier.testTag("confirm_delete_project_button")
                ) {
                    Text("Delete", color = ShinobiRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        modifier = Modifier.testTag("edit_project_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit Project",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_project_input_name"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = techStack,
                onValueChange = { techStack = it },
                label = { Text("Tech Stack") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_project_input_tech_stack"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("edit_project_dropdown_status")
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    ProjectStatus.values().forEach { s ->
                        val color = getProjectStatusColor(s)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = s.name.replace('_', ' '),
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                status = s
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = repoLink,
                onValueChange = { repoLink = it },
                label = { Text("Repository Link (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_project_input_repo_link"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    border = BorderStroke(1.dp, ShinobiRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ShinobiRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("delete_project_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onUpdate(name, techStack, status, repoLink)
                        }
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("save_edit_project_button")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
