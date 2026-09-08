package com.example.feature.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusCategory

fun formatTimerDisplay(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun getCategoryIcon(category: FocusCategory): ImageVector = when (category) {
    FocusCategory.STUDY -> Icons.Default.MenuBook
    FocusCategory.WORKOUT -> Icons.Default.FitnessCenter
    FocusCategory.CUSTOM -> Icons.Default.Tune
}

fun getCategoryColor(category: FocusCategory): Color = when (category) {
    FocusCategory.STUDY -> Color(0xFF42A5F5)    // Blue
    FocusCategory.WORKOUT -> Color(0xFFFF7043)  // Orange / Coral
    FocusCategory.CUSTOM -> Color(0xFFAB47BC)   // Purple
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val customNote by viewModel.customNote.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val todaySummary by viewModel.todaySummary.collectAsState()

    val activeCategoryColor = getCategoryColor(selectedCategory)

    // Pulse animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (timerState == TimerState.RUNNING) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus Timer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("focus_header_title")
                    )
                    Text(
                        text = "Deep work sessions & daily tracking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = activeCategoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(selectedCategory),
                            contentDescription = null,
                            tint = activeCategoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedCategory.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeCategoryColor
                        )
                    }
                }
            }

            // Category Picker Row (Study / Workout / Custom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FocusCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    val categoryColor = getCategoryColor(category)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (timerState == TimerState.IDLE) {
                                viewModel.selectCategory(category)
                            }
                        },
                        enabled = timerState == TimerState.IDLE,
                        label = {
                            Text(
                                text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = categoryColor.copy(alpha = 0.2f),
                            selectedLabelColor = categoryColor,
                            selectedLeadingIconColor = categoryColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_category_${category.name.lowercase()}")
                    )
                }
            }

            // Custom note field if CUSTOM category is selected
            AnimatedVisibility(visible = selectedCategory == FocusCategory.CUSTOM) {
                OutlinedTextField(
                    value = customNote,
                    onValueChange = { viewModel.updateCustomNote(it) },
                    label = { Text("Session Note (Optional)") },
                    placeholder = { Text("e.g. Side project, Reading, Meditation") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_focus_note"),
                    singleLine = true
                )
            }

            // Large Timer Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("timer_display_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = BorderStroke(
                    width = if (timerState == TimerState.RUNNING) 2.dp else 1.dp,
                    color = if (timerState == TimerState.RUNNING) activeCategoryColor else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Circular indicator ring backdrop
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(200.dp)
                            .scale(pulseScale)
                            .background(
                                color = activeCategoryColor.copy(alpha = if (timerState == TimerState.RUNNING) 0.12f else 0.05f),
                                shape = CircleShape
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formatTimerDisplay(elapsedSeconds),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 44.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = if (timerState == TimerState.RUNNING) activeCategoryColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("text_timer_digits")
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = when (timerState) {
                                    TimerState.IDLE -> "READY"
                                    TimerState.RUNNING -> "FOCUSING"
                                    TimerState.PAUSED -> "PAUSED"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (timerState) {
                                    TimerState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    TimerState.RUNNING -> activeCategoryColor
                                    TimerState.PAUSED -> Color(0xFFFFA000)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Timer Controls (Start / Pause / Stop)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (timerState) {
                            TimerState.IDLE -> {
                                FilledIconButton(
                                    onClick = { viewModel.startTimer() },
                                    modifier = Modifier
                                        .size(68.dp)
                                        .testTag("btn_timer_start"),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = activeCategoryColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start Timer",
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            TimerState.RUNNING -> {
                                FilledIconButton(
                                    onClick = { viewModel.pauseTimer() },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .testTag("btn_timer_pause"),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color(0xFFFFA000),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause Timer",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                FilledIconButton(
                                    onClick = { viewModel.stopAndSaveTimer() },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .testTag("btn_timer_stop"),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop & Save",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            TimerState.PAUSED -> {
                                FilledIconButton(
                                    onClick = { viewModel.startTimer() },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .testTag("btn_timer_resume"),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = activeCategoryColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume Timer",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                FilledIconButton(
                                    onClick = { viewModel.stopAndSaveTimer() },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .testTag("btn_timer_stop"),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop & Save",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Today's Focus Summary Card
            TodayFocusCard(summary = todaySummary)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TodayFocusCard(
    summary: TodayFocusSummary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_focus_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Focus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${summary.totalMinutes} min total",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown Per Category: Study / Workout / Custom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CategoryStatItem(
                    title = "Study",
                    minutes = summary.studyMinutes,
                    color = getCategoryColor(FocusCategory.STUDY),
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.testTag("summary_study_minutes")
                )

                CategoryStatItem(
                    title = "Workout",
                    minutes = summary.workoutMinutes,
                    color = getCategoryColor(FocusCategory.WORKOUT),
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.testTag("summary_workout_minutes")
                )

                CategoryStatItem(
                    title = "Custom",
                    minutes = summary.customMinutes,
                    color = getCategoryColor(FocusCategory.CUSTOM),
                    icon = Icons.Default.Tune,
                    modifier = Modifier.testTag("summary_custom_minutes")
                )
            }
        }
    }
}

@Composable
fun CategoryStatItem(
    title: String,
    minutes: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$minutes min",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
