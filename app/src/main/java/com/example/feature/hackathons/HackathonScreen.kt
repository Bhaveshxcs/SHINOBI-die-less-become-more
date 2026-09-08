package com.example.feature.hackathons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HackathonEntry
import com.example.data.HackathonStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

// Color mapping for Hackathon Status
fun getStatusColor(status: HackathonStatus): Color = when (status) {
    HackathonStatus.REGISTERED -> Color(0xFF2196F3)  // Blue
    HackathonStatus.IN_PROGRESS -> Color(0xFFFFA000) // Amber
    HackathonStatus.SUBMITTED -> Color(0xFF4CAF50)   // Green
    HackathonStatus.MISSED -> Color(0xFFE53935)      // Red
}

fun formatEpochDate(millis: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun getDaysRemaining(deadlineMillis: Long): Long {
    val diff = deadlineMillis - System.currentTimeMillis()
    return ceil(diff.toDouble() / (1000 * 60 * 60 * 24)).toLong()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonScreen(
    viewModel: HackathonViewModel,
    modifier: Modifier = Modifier
) {
    val hackathons by viewModel.hackathons.collectAsState()
    val isAddSheetOpen by viewModel.isAddSheetOpen.collectAsState()
    val selectedHackathon by viewModel.selectedHackathon.collectAsState()

    val currentTime = remember { System.currentTimeMillis() }
    val threeDaysMillis = 3L * 24 * 60 * 60 * 1000L

    // Warning list: deadline within 3 days and status is not SUBMITTED
    val urgentEntries = remember(hackathons, currentTime) {
        hackathons.filter {
            it.status != HackathonStatus.SUBMITTED &&
                (it.deadlineDate - currentTime) <= threeDaysMillis
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                modifier = Modifier.testTag("fab_add_hackathon"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Hackathon")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hackathons",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("hackathons_header_title")
                    )
                    Text(
                        text = "${hackathons.size} tracked competitions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.openAddSheet() },
                    modifier = Modifier.testTag("btn_add_hackathon_header")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add")
                }
            }

            // High Priority Warning Banner if any hackathon is due within 3 days and not submitted
            if (urgentEntries.isNotEmpty()) {
                UrgentWarningCard(
                    urgentList = urgentEntries,
                    onEntryClick = { viewModel.selectHackathon(it) }
                )
            }

            if (hackathons.isEmpty()) {
                EmptyHackathonView(onAddClick = { viewModel.openAddSheet() })
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(hackathons, key = { it.id }) { item ->
                        HackathonCard(
                            entry = item,
                            onClick = { viewModel.selectHackathon(item) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Hackathon Bottom Sheet
    if (isAddSheetOpen) {
        AddHackathonBottomSheet(
            onDismiss = { viewModel.closeAddSheet() },
            onSave = { name, status, regDate, deadlineDate, link, learnings ->
                viewModel.addHackathon(name, status, regDate, deadlineDate, link, learnings)
            }
        )
    }

    // Edit/Detail Bottom Sheet
    selectedHackathon?.let { entry ->
        EditHackathonBottomSheet(
            entry = entry,
            onDismiss = { viewModel.selectHackathon(null) },
            onUpdate = { updated -> viewModel.updateHackathon(updated) },
            onDelete = { toDelete -> viewModel.deleteHackathon(toDelete) }
        )
    }
}

@Composable
fun UrgentWarningCard(
    urgentList: List<HackathonEntry>,
    onEntryClick: (HackathonEntry) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("urgent_deadline_warning_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3E1F1F) // Deep warning red/brown container
        ),
        border = BorderStroke(1.5.dp, Color(0xFFEF5350)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Urgent Warning",
                    tint = Color(0xFFFF7043),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "URGENT SUBMISSION DEADLINE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF8A80)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${urgentList.size} hackathon(s) have deadlines within 3 days and are NOT submitted yet! Make sure to finalize your build and submit in time:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFCDD2)
            )

            Spacer(modifier = Modifier.height(10.dp))

            urgentList.forEach { entry ->
                val daysLeft = getDaysRemaining(entry.deadlineDate)
                val deadlineText = when {
                    daysLeft < 0 -> "OVERDUE by ${-daysLeft} day(s)"
                    daysLeft == 0L -> "DUE TODAY!"
                    daysLeft == 1L -> "Due tomorrow (${formatEpochDate(entry.deadlineDate)})"
                    else -> "Due in $daysLeft days (${formatEpochDate(entry.deadlineDate)})"
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onEntryClick(entry) },
                    color = Color(0xFF4C2424),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = deadlineText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (daysLeft <= 1) Color(0xFFFF5252) else Color(0xFFFFAB91)
                            )
                        }

                        StatusBadge(status = entry.status)
                    }
                }
            }
        }
    }
}

@Composable
fun HackathonCard(
    entry: HackathonEntry,
    onClick: () -> Unit
) {
    val daysLeft = getDaysRemaining(entry.deadlineDate)
    val isNearDeadline = entry.status != HackathonStatus.SUBMITTED && daysLeft <= 3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("hackathon_item_${entry.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isNearDeadline) BorderStroke(1.dp, Color(0xFFEF5350)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusBadge(status = entry.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Deadline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatEpochDate(entry.deadlineDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNearDeadline) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                    )
                }

                val daysBadgeText = when {
                    entry.status == HackathonStatus.SUBMITTED -> "Submitted"
                    daysLeft < 0 -> "${-daysLeft}d overdue"
                    daysLeft == 0L -> "Due today"
                    daysLeft == 1L -> "Due tomorrow"
                    else -> "$daysLeft days left"
                }

                Text(
                    text = daysBadgeText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isNearDeadline) Color(0xFFFF7043) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!entry.projectLink.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Project: ${entry.projectLink}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: HackathonStatus) {
    val color = getStatusColor(status)
    val label = when (status) {
        HackathonStatus.REGISTERED -> "REGISTERED"
        HackathonStatus.IN_PROGRESS -> "IN PROGRESS"
        HackathonStatus.SUBMITTED -> "SUBMITTED"
        HackathonStatus.MISSED -> "MISSED"
    }

    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyHackathonView(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No Hackathons Tracked",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first upcoming competition to keep deadlines, project links, and learnings organized.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Hackathon")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHackathonBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, status: HackathonStatus, regDate: Long, deadlineDate: Long, link: String?, learnings: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(HackathonStatus.REGISTERED) }

    val now = remember { System.currentTimeMillis() }
    var registrationDate by remember { mutableLongStateOf(now) }
    // Default deadline: 7 days from now
    var deadlineDate by remember { mutableLongStateOf(now + (7L * 24 * 60 * 60 * 1000L)) }

    var projectLink by remember { mutableStateOf("") }
    var learnings by remember { mutableStateOf("") }

    var isSelectingDeadline by remember { mutableStateOf(false) }
    var isSelectingRegDate by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add New Hackathon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Hackathon Name *") },
                placeholder = { Text("e.g. Gemini AI Hackathon 2026") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_hackathon_name"),
                singleLine = true
            )

            // Status Picker
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = status.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dropdown_status")
                )
                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded,
                    onDismissRequest = { statusDropdownExpanded = false }
                ) {
                    HackathonStatus.values().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name.replace("_", " ")) },
                            onClick = {
                                status = item
                                statusDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Registration Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Registration Date", style = MaterialTheme.typography.labelMedium)
                    Text(formatEpochDate(registrationDate), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
                FilledTonalButton(onClick = { isSelectingRegDate = true }) {
                    Text("Change")
                }
            }

            // Deadline Date Row + Quick Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Deadline Date *", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatEpochDate(deadlineDate),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    FilledTonalButton(
                        onClick = { isSelectingDeadline = true },
                        modifier = Modifier.testTag("btn_change_deadline")
                    ) {
                        Text("Pick Date")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick deadline chips (+3 days, +1 week, +2 weeks, +1 month)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "3 Days" to (3L * 86400000L),
                        "1 Week" to (7L * 86400000L),
                        "2 Weeks" to (14L * 86400000L),
                        "1 Month" to (30L * 86400000L)
                    ).forEach { (label, duration) ->
                        OutlinedButton(
                            onClick = { deadlineDate = now + duration },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = projectLink,
                onValueChange = { projectLink = it },
                label = { Text("Project / Repo Link (Optional)") },
                placeholder = { Text("https://github.com/...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = learnings,
                onValueChange = { learnings = it },
                label = { Text("Learnings / Notes (Optional)") },
                placeholder = { Text("Key technologies used, mentor advice, takeaways...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    onSave(name, status, registrationDate, deadlineDate, projectLink, learnings)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_submit_hackathon")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Hackathon")
            }
        }
    }

    if (isSelectingRegDate) {
        HackathonDatePickerDialog(
            initialMillis = registrationDate,
            onDismiss = { isSelectingRegDate = false },
            onDateSelected = {
                registrationDate = it
                isSelectingRegDate = false
            }
        )
    }

    if (isSelectingDeadline) {
        HackathonDatePickerDialog(
            initialMillis = deadlineDate,
            onDismiss = { isSelectingDeadline = false },
            onDateSelected = {
                deadlineDate = it
                isSelectingDeadline = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHackathonBottomSheet(
    entry: HackathonEntry,
    onDismiss: () -> Unit,
    onUpdate: (HackathonEntry) -> Unit,
    onDelete: (HackathonEntry) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(entry.name) }
    var status by remember { mutableStateOf(entry.status) }
    var registrationDate by remember { mutableLongStateOf(entry.registrationDate) }
    var deadlineDate by remember { mutableLongStateOf(entry.deadlineDate) }
    var projectLink by remember { mutableStateOf(entry.projectLink ?: "") }
    var learnings by remember { mutableStateOf(entry.learnings ?: "") }

    var isSelectingDeadline by remember { mutableStateOf(false) }
    var isSelectingRegDate by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hackathon Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Hackathon Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_hackathon_name"),
                singleLine = true
            )

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = status.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("edit_status_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded,
                    onDismissRequest = { statusDropdownExpanded = false }
                ) {
                    HackathonStatus.values().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name.replace("_", " ")) },
                            onClick = {
                                status = item
                                statusDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Registration Date", style = MaterialTheme.typography.labelMedium)
                    Text(formatEpochDate(registrationDate), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
                FilledTonalButton(onClick = { isSelectingRegDate = true }) {
                    Text("Change")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Deadline Date", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatEpochDate(deadlineDate),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                FilledTonalButton(onClick = { isSelectingDeadline = true }) {
                    Text("Pick Date")
                }
            }

            OutlinedTextField(
                value = projectLink,
                onValueChange = { projectLink = it },
                label = { Text("Project Link") },
                placeholder = { Text("https://github.com/...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = learnings,
                onValueChange = { learnings = it },
                label = { Text("Learnings & Retrospective") },
                placeholder = { Text("What did you build? What were the challenges?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_learnings_field"),
                minLines = 4
            )

            // Save & Delete Buttons
            Button(
                onClick = {
                    onUpdate(
                        entry.copy(
                            name = name.trim(),
                            status = status,
                            registrationDate = registrationDate,
                            deadlineDate = deadlineDate,
                            projectLink = projectLink.takeIf { it.isNotBlank() }?.trim(),
                            learnings = learnings.takeIf { it.isNotBlank() }?.trim()
                        )
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_save_hackathon_changes")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes")
            }

            FilledTonalButton(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_delete_hackathon")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Hackathon")
            }
        }
    }

    if (isSelectingRegDate) {
        HackathonDatePickerDialog(
            initialMillis = registrationDate,
            onDismiss = { isSelectingRegDate = false },
            onDateSelected = {
                registrationDate = it
                isSelectingRegDate = false
            }
        )
    }

    if (isSelectingDeadline) {
        HackathonDatePickerDialog(
            initialMillis = deadlineDate,
            onDismiss = { isSelectingDeadline = false },
            onDateSelected = {
                deadlineDate = it
                isSelectingDeadline = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Hackathon?") },
            text = { Text("Are you sure you want to delete '${entry.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(entry)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonDatePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) } ?: onDismiss()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
