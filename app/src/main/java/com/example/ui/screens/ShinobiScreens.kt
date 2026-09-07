package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShinobiViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShinobiMainApp(viewModel: ShinobiViewModel) {
    val profile by viewModel.profile.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    val workouts by viewModel.workouts.collectAsState()
    val studySessions by viewModel.studySessions.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val apiKeyWarning by viewModel.apiKeyWarning.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (profile == null) {
            // Un-onboarded profile: Show Onboarding Steps
            OnboardingScreen(onOnboard = { name, goals, weaknesses, wake, sleep, commit ->
                viewModel.onboardUser(name, goals, weaknesses, wake, sleep, commit)
            })
        } else {
            val userProfile = profile!!
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Shinobi Star",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SHINOBI",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        actions = {
                            if (apiKeyWarning != null) {
                                IconButton(
                                    onClick = { showSettingsDialog = true },
                                    modifier = Modifier.testTag("api_key_warning")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "API key missing annotation",
                                        tint = AmberGold
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showSettingsDialog = true },
                                modifier = Modifier.testTag("btn_dojo_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Dojo Credentials Settings",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") },
                            modifier = Modifier.testTag("nav_dashboard")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Body") },
                            label = { Text("Body") },
                            modifier = Modifier.testTag("nav_body")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Info, contentDescription = "Mind") },
                            label = { Text("Mind") },
                            modifier = Modifier.testTag("nav_mind")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.Send, contentDescription = "Mentor Chat") },
                            label = { Text("Mentor") },
                            modifier = Modifier.testTag("nav_mentor")
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Small overlay if API key warning is present
                        apiKeyWarning?.let {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Warn", tint = AmberGold)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        when (selectedTab) {
                            0 -> DashboardScreen(
                                profile = userProfile,
                                checkIns = checkIns,
                                triggerCheckIn = { showCheckInDialog = true },
                                onForceSynthesis = {
                                    viewModel.triggerWeeklySynthesis()
                                    selectedTab = 3 // Route to chat to view report
                                }
                            )
                            1 -> FitnessScreen(
                                workouts = workouts,
                                onGenerateWorkout = { title, equip, mins, diff ->
                                    viewModel.requestCustomWorkout(title, equip, mins, diff)
                                },
                                onSkipWorkout = { id, reason -> viewModel.skipCurrentWorkout(id, reason) },
                                onCompleteWorkout = { id -> viewModel.completeCurrentWorkout(id) },
                                onCompleteMinimumViable = { id -> viewModel.completeMinimumViableWorkout(id) },
                                isGenerating = isGenerating
                            )
                            2 -> StudyScreen(
                                studySessions = studySessions,
                                onAddStudySession = { sub, target, planned, actual, learned, confused, review ->
                                    viewModel.addManualStudySession(sub, target, planned, actual, learned, confused, review)
                                },
                                onExplainConcept = { concept ->
                                    viewModel.requestConceptCheck(concept)
                                    selectedTab = 3 // Route to chat to read explanation
                                },
                                isGenerating = isGenerating
                            )
                            3 -> MentorScreen(
                                chatHistory = chatHistory,
                                onSendMessage = { msg -> viewModel.submitChatMessage(msg) },
                                onClearChat = { viewModel.clearChatLogs() },
                                onGenerateReport = { viewModel.triggerWeeklySynthesis() },
                                isGenerating = isGenerating
                            )
                        }
                    }

                    if (showCheckInDialog) {
                        CheckInDialog(
                            onDismiss = { showCheckInDialog = false },
                            onSubmit = { completed, skips, energy, focus, mood, honest, reason ->
                                viewModel.submitCheckIn(completed, skips, energy, focus, mood, honest, reason)
                                showCheckInDialog = false
                            }
                        )
                    }

                    if (showSettingsDialog) {
                        ApiSettingsDialog(
                            profile = userProfile,
                            onDismiss = { showSettingsDialog = false },
                            onSave = { key, search ->
                                viewModel.updateApiSettings(key, search)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(onOnboard: (String, String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var weaknesses by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("06:00 AM") }
    var sleepTime by remember { mutableStateOf("10:30 PM") }
    var firstCommitment by remember { mutableStateOf("") }

    var step by remember { mutableIntStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual symbol
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Logo",
                tint = ShinobiRed,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = "SHINOBI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                letterSpacing = 4.sp
            )
            Text(
                text = "No lies, no shame, only discipline.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            LinearProgressIndicator(
                progress = { step / 3.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                color = ShinobiRed,
                trackColor = BorderColor
            )

            Crossfade(targetState = step, label = "OnboardingCrossfade") { currentStep ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (currentStep) {
                        1 -> {
                            Text(
                                text = "1. Identity & Objective",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "What are you trying to become? Stating goals is cheap, but it establishes the line of fire.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("What is your name?") },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .testTag("input_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ShinobiRed,
                                    unfocusedBorderColor = BorderColor
                                )
                            )

                            OutlinedTextField(
                                value = goals,
                                onValueChange = { goals = it },
                                label = { Text("What is your absolute goal? (e.g. Pass CS fundamentals, double muscular strength)") },
                                placeholder = { Text("Be specific, not general.") },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_goals"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ShinobiRed,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                        }
                        2 -> {
                            Text(
                                text = "2. The Honest Mirror",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "State your primary self-defeating weaknesses. Shinobi uses this to watch your patterns. Avoidance, inconsistency, fatigue, video game binging?",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = weaknesses,
                                onValueChange = { weaknesses = it },
                                label = { Text("Your exact personal weaknesses") },
                                placeholder = { Text("Avoidance on Mondays, giving up when tired, over-scrolling...") },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                minLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_weaknesses"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ShinobiRed,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                        }
                        3 -> {
                            Text(
                                text = "3. Structure & First Commitment",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Let's review schedule constraints and set your first immediate physical or mental commitment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = wakeTime,
                                    onValueChange = { wakeTime = it },
                                    label = { Text("Wake") },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                        .testTag("input_wake"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ShinobiRed,
                                        unfocusedBorderColor = BorderColor
                                    )
                                )

                                OutlinedTextField(
                                    value = sleepTime,
                                    onValueChange = { sleepTime = it },
                                    label = { Text("Sleep") },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                        .testTag("input_sleep"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ShinobiRed,
                                        unfocusedBorderColor = BorderColor
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = firstCommitment,
                                onValueChange = { firstCommitment = it },
                                label = { Text("Define one simple action for next 24 hour shift:") },
                                placeholder = { Text("e.g. Do 10 pushups, read 3 pages of recursion chapter") },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_commitment"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ShinobiRed,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    TextButton(
                        onClick = { step-- },
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Text("BACK", color = TextSecondary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            if (name.isNotBlank() && goals.isNotBlank() && weaknesses.isNotBlank() && firstCommitment.isNotBlank()) {
                                onOnboard(name, goals, weaknesses, wakeTime, sleepTime, firstCommitment)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                    modifier = Modifier.testTag("btn_next")
                ) {
                    Text(
                        text = if (step == 3) "ENTER THE TRAINING HALL" else "NEXT",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    profile: UserProfile,
    checkIns: List<DailyCheckIn>,
    triggerCheckIn: () -> Unit,
    onForceSynthesis: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcoming Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "WELCOME BACK, SHINOBI ${profile.name.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your path: ${profile.goals}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Immediate commitment: ${profile.activePlan}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Progression Bars
        Text(
            text = "PROGRESSION AXES",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AttributeRow(name = "Discipline (Streaks & Honesty)", value = profile.discipline, max = 5, color = AmberGold, desc = "Genin -> Chunin -> Jonin stages")
                Spacer(modifier = Modifier.height(12.dp))
                AttributeRow(name = "Mind / Knowledge (Study logs)", value = profile.knowledge, max = 5, color = SkyTeal, desc = "Deep focus vs surface-level reading")
                Spacer(modifier = Modifier.height(12.dp))
                AttributeRow(name = "Body (Workout completeness)", value = profile.body, max = 5, color = MintGreen, desc = "Power level of physical training")
                Spacer(modifier = Modifier.height(12.dp))
                AttributeRow(name = "Awareness (Insightful debriefs)", value = profile.awareness, max = 5, color = MysticIndigo, desc = "Self-deception prevention metric")
            }
        }

        // Action Hub Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val alreadyCheckedIn = profile.lastCheckInDate == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            Button(
                onClick = { triggerCheckIn() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (alreadyCheckedIn) MaterialTheme.colorScheme.surfaceVariant else ShinobiRed
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_trigger_checkin")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Check",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (alreadyCheckedIn) "DAILY LOG DEBRIEFED" else "DAILY DEBRIEF NOW",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { onForceSynthesis() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_weekly_synthesis")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WEEKLY REPORT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Weakness Mirror card
        Text(
            text = "THE MIRROR OF WEAKNESS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Mirror Warning",
                    tint = ShinobiRed,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Identified Impediments:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ShinobiRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.weaknesses,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        // Stats checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Honesty Streaks: ${profile.streakOfHonestyFlags} days",
                    fontWeight = FontWeight.Bold,
                    color = MintGreen,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The shinobi does not feed stats to please the app. Total registered logs: ${checkIns.size}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AttributeRow(name: String, value: Int, max: Int, color: Color, desc: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = "Stage $value/$max", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..max) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (i <= value) color else BorderColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun FitnessScreen(
    workouts: List<WorkoutSession>,
    onGenerateWorkout: (String, String, Int, String) -> Unit,
    onSkipWorkout: (Int, String) -> Unit,
    onCompleteWorkout: (Int) -> Unit,
    onCompleteMinimumViable: (Int) -> Unit,
    isGenerating: Boolean
) {
    var showGeneratorForm by remember { mutableStateOf(false) }

    var workoutName by remember { mutableStateOf("Stamina & Core Conditioning") }
    var availableEquip by remember { mutableStateOf("Bodyweight and resistance band") }
    var durationMinutes by remember { mutableStateOf(30) }
    var selectionDifficulty by remember { mutableStateOf("Medium") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BODY - PHYSICAL DISCIPLINE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Physical strength is the foundation of mental control. A heavy body clouding the intellect is a critical failure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showGeneratorForm = !showGeneratorForm },
                        colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_toggle_generator")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GENERATE PERSONALIZE WORKOUT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showGeneratorForm) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CUSTOM TRAINING TEMPLATE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = workoutName,
                            onValueChange = { workoutName = it },
                            label = { Text("Workout Name / Focus") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_workout_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = availableEquip,
                            onValueChange = { availableEquip = it },
                            label = { Text("Available Equipment") },
                            placeholder = { Text("Bodyweight, resistance bands, pullup bar...") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_workout_equip"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Duration: $durationMinutes minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Slider(
                            value = durationMinutes.toFloat(),
                            onValueChange = { durationMinutes = it.toInt() },
                            valueRange = 10f..90f,
                            steps = 7,
                            modifier = Modifier.testTag("form_workout_duration"),
                            colors = SliderDefaults.colors(
                                thumbColor = ShinobiRed,
                                activeTrackColor = ShinobiRed
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Difficulty: $selectionDifficulty",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Easy", "Medium", "Hard").forEach { diff ->
                                OutlinedButton(
                                    onClick = { selectionDifficulty = diff },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("diff_btn_$diff"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectionDifficulty == diff) ShinobiRed else Color.Transparent
                                    ),
                                    border = BorderStroke(1.dp, if (selectionDifficulty == diff) ShinobiRed else BorderColor)
                                ) {
                                    Text(text = diff, color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onGenerateWorkout(workoutName, availableEquip, durationMinutes, selectionDifficulty)
                                showGeneratorForm = false
                            },
                            enabled = !isGenerating && workoutName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_submit_generator")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("SUMMON SHINOBI DRILL PLAN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active drills
        val activeWorkouts = workouts.filter { it.status == "PLANNED" }
        if (activeWorkouts.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE PHYSICAL ASSIGNMENTS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ShinobiRed,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(activeWorkouts) { workout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, ShinobiRed)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = workout.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ShinobiRedMuted)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DRAFT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = workout.exercises,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground)
                                .padding(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Action rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onCompleteWorkout(workout.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_complete_${workout.id}")
                            ) {
                                Text("COMPLETED", fontWeight = FontWeight.Bold, color = DarkBackground)
                            }

                            Button(
                                onClick = { onCompleteMinimumViable(workout.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("btn_mvp_${workout.id}")
                            ) {
                                Text("MINIMUM MOVE (PULLUPS/PUSHUPS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onSkipWorkout(workout.id, "Tired / Avoidance") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_skip_${workout.id}"),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("SKIP THIS DRILL", color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Historic workouts list
        val historicWorkouts = workouts.filter { it.status != "PLANNED" }
        if (historicWorkouts.isNotEmpty()) {
            item {
                Text(
                    text = "HISTORIC PHYSICAL DISCIPLINE LOGS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(historicWorkouts) { workout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = workout.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = workout.dateString, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }

                        val statusColor = when (workout.status) {
                            "COMPLETED" -> MintGreen
                            "MINIMUM_VIABLE" -> AmberGold
                            else -> ShinobiRed
                        }

                        Text(
                            text = workout.status,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyScreen(
    studySessions: List<StudySession>,
    onAddStudySession: (String, String, Int, Int, String, String, String) -> Unit,
    onExplainConcept: (String) -> Unit,
    isGenerating: Boolean
) {
    val presets = listOf(15, 25, 45, 60, 90)
    var selectedPresetMinutes by remember { mutableIntStateOf(25) }
    var customMinutesInput by remember { mutableStateOf("") }
    var isCustomDuration by remember { mutableStateOf(false) }

    val studyTechniques = listOf("Pomodoro", "Deep Focus", "Active Recall", "Feynman Drill")
    var selectedTechnique by remember { mutableStateOf("Pomodoro") }

    var isTimerRunning by remember { mutableStateOf(false) }
    var timeLeftSeconds by remember { mutableIntStateOf(25 * 60) }
    var plannedSeconds by remember { mutableIntStateOf(25 * 60) }

    var isSessionActive by remember { mutableStateOf(false) }
    var currentSubject by remember { mutableStateOf("") }
    var targetObjective by remember { mutableStateOf("") }

    var showDebriefDialog by remember { mutableStateOf(false) }
    var debriefSubject by remember { mutableStateOf("") }
    var debriefTarget by remember { mutableStateOf("") }
    var debriefPlannedMin by remember { mutableIntStateOf(25) }
    var debriefActualMin by remember { mutableIntStateOf(25) }

    var whatLearned by remember { mutableStateOf("") }
    var whatConfused by remember { mutableStateOf("") }
    var tomorrowFocus by remember { mutableStateOf("") }

    var explanationQuery by remember { mutableStateOf("") }

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayStudyMinutes = remember(studySessions) {
        studySessions.filter { it.dateString == todayDateStr }.sumOf { it.actualMinutes }
    }

    // Timer countdown effect
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
        }
        if (timeLeftSeconds == 0 && isTimerRunning) {
            isTimerRunning = false
            debriefSubject = if (currentSubject.isNotBlank()) currentSubject else "Deep Focus ($selectedTechnique)"
            debriefTarget = if (targetObjective.isNotBlank()) targetObjective else "Targeted study block"
            debriefPlannedMin = plannedSeconds / 60
            debriefActualMin = plannedSeconds / 60
            showDebriefDialog = true
            isSessionActive = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STUDY FOCUS CHAMBER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Train the mind with ruthless focus blocks & immediate debriefs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Focus", tint = AmberGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today's Focus: ${todayStudyMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "${studySessions.size} total sessions logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyTeal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // ACTIVE STUDY TIMER OR SETUP
        if (!isSessionActive && !showDebriefDialog) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.5.dp, ShinobiRed.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STUDY TIMER SETUP",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )

                            Text(
                                text = "${if (isCustomDuration) (customMinutesInput.toIntOrNull() ?: 25) else selectedPresetMinutes} MIN",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ShinobiRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Duration Presets Row
                        Text(
                            text = "SELECT DURATION BLOCK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.forEach { mins ->
                                val isSelected = !isCustomDuration && selectedPresetMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        isCustomDuration = false
                                        selectedPresetMinutes = mins
                                    },
                                    label = { Text("${mins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ShinobiRed,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom Duration Option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = isCustomDuration,
                                onClick = { isCustomDuration = true },
                                label = { Text("Custom", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SkyTeal,
                                    selectedLabelColor = DarkBackground
                                )
                            )
                            if (isCustomDuration) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = customMinutesInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } && input.length <= 3) {
                                            customMinutesInput = input
                                        }
                                    },
                                    placeholder = { Text("Min (e.g. 50)") },
                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(140.dp)
                                        .testTag("custom_minutes_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SkyTeal,
                                        unfocusedBorderColor = BorderColor
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Technique Chips
                        Text(
                            text = "FOCUS METHODOLOGY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            studyTechniques.forEach { tech ->
                                val isSelected = selectedTechnique == tech
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTechnique = tech },
                                    label = { Text(tech, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmberGold.copy(alpha = 0.25f),
                                        selectedLabelColor = AmberGold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = currentSubject,
                            onValueChange = { currentSubject = it },
                            label = { Text("Subject / Topic") },
                            placeholder = { Text("e.g. Data Structures, Cell Biology, Microeconomics") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("study_subject_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = targetObjective,
                            onValueChange = { targetObjective = it },
                            label = { Text("Target Objective / Key Question") },
                            placeholder = { Text("e.g. Master balancing Red-Black trees & prove runtime") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("study_target_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        val resolvedMinutes = if (isCustomDuration) {
                            (customMinutesInput.toIntOrNull() ?: 25).coerceIn(1, 300)
                        } else {
                            selectedPresetMinutes
                        }

                        Button(
                            onClick = {
                                val subj = if (currentSubject.isNotBlank()) currentSubject.trim() else "Deep Study Block"
                                val target = if (targetObjective.isNotBlank()) targetObjective.trim() else "Cognitive immersion ($selectedTechnique)"
                                currentSubject = subj
                                targetObjective = target
                                plannedSeconds = resolvedMinutes * 60
                                timeLeftSeconds = resolvedMinutes * 60
                                isTimerRunning = true
                                isSessionActive = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_start_pomodoro")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Timer", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COMMENCE $resolvedMinutes-MIN STUDY TIMER", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        } else if (isSessionActive) {
            // Screen representation of active study timer
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, if (isTimerRunning) ShinobiRed else AmberGold)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTimerRunning) ShinobiRedMuted else AmberGold.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isTimerRunning) "● IMMERSION ACTIVE ($selectedTechnique)" else "⏸ STUDY TIMER PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTimerRunning) ShinobiRed else AmberGold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentSubject,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target: $targetObjective",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large Digital Countdown Clock
                        val hoursLeft = timeLeftSeconds / 3600
                        val minutesLeft = (timeLeftSeconds % 3600) / 60
                        val secondsLeft = timeLeftSeconds % 60
                        val formattedTime = if (hoursLeft > 0) {
                            String.format("%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft)
                        } else {
                            String.format("%02d:%02d", minutesLeft, secondsLeft)
                        }

                        Text(
                            text = formattedTime,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (timeLeftSeconds <= 60 && isTimerRunning) ShinobiRed else TextPrimary,
                            letterSpacing = 2.sp,
                            modifier = Modifier.testTag("pomodoro_text_timer")
                        )

                        val progressFraction = if (plannedSeconds > 0) {
                            (timeLeftSeconds / plannedSeconds.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val elapsedMinutes = (plannedSeconds - timeLeftSeconds) / 60
                        val totalMinutes = plannedSeconds / 60

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$elapsedMinutes of $totalMinutes min elapsed",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (timeLeftSeconds <= 60) ShinobiRed else MintGreen,
                            trackColor = BorderColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick Timer Extensions Row (+1m, +5m)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = {
                                    timeLeftSeconds += 60
                                    plannedSeconds += 60
                                },
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "+1m", modifier = Modifier.size(14.dp), tint = SkyTeal)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+1 Min", fontSize = 12.sp, color = SkyTeal)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = {
                                    timeLeftSeconds += 300
                                    plannedSeconds += 300
                                },
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "+5m", modifier = Modifier.size(14.dp), tint = MintGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+5 Min", fontSize = 12.sp, color = MintGreen)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = {
                                    timeLeftSeconds = plannedSeconds
                                    isTimerRunning = false
                                },
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(14.dp), tint = TextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isTimerRunning = !isTimerRunning },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTimerRunning) AmberGold else MintGreen
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_pause_study")
                            ) {
                                Icon(
                                    if (isTimerRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle Pause",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isTimerRunning) "PAUSE" else "RESUME",
                                    color = DarkBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    isTimerRunning = false
                                    debriefSubject = currentSubject
                                    debriefTarget = targetObjective
                                    debriefPlannedMin = plannedSeconds / 60
                                    debriefActualMin = ((plannedSeconds - timeLeftSeconds) / 60).coerceAtLeast(1)
                                    showDebriefDialog = true
                                    isSessionActive = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("btn_terminate_study")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "End & Debrief", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("END & DEBRIEF", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showDebriefDialog) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.5.dp, MintGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STUDY SESSION DEBRIEF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MintGreen,
                                fontFamily = FontFamily.Monospace
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MintGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${debriefActualMin}m Logged",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MintGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Subject: $debriefSubject",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Target: $debriefTarget",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = whatLearned,
                            onValueChange = { whatLearned = it },
                            label = { Text("What did you actually understand / internalize?") },
                            placeholder = { Text("Summarize core concept takeaways in your own words...") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("debrief_learned_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = whatConfused,
                            onValueChange = { whatConfused = it },
                            label = { Text("What was confusing, resisted, or unclear?") },
                            placeholder = { Text("Be brutally honest about cognitive friction...") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("debrief_confused_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = tomorrowFocus,
                            onValueChange = { tomorrowFocus = it },
                            label = { Text("Immediate next review focus point:") },
                            placeholder = { Text("A highly targeted next action step...") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("debrief_tomorrow_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showDebriefDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Discard", color = TextSecondary)
                            }

                            Button(
                                onClick = {
                                    val learned = if (whatLearned.isNotBlank()) whatLearned.trim() else "Study block completed without notes."
                                    val nextStep = if (tomorrowFocus.isNotBlank()) tomorrowFocus.trim() else "Follow-up review."
                                    onAddStudySession(
                                        debriefSubject,
                                        debriefTarget,
                                        debriefPlannedMin,
                                        debriefActualMin,
                                        learned,
                                        whatConfused.trim(),
                                        nextStep
                                    )
                                    // Reset active variables
                                    whatLearned = ""
                                    whatConfused = ""
                                    tomorrowFocus = ""
                                    showDebriefDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                modifier = Modifier
                                    .weight(2f)
                                    .testTag("btn_submit_debrief")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Transmit", tint = DarkBackground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVE DEBRIEF", fontWeight = FontWeight.Bold, color = DarkBackground)
                            }
                        }
                    }
                }
            }
        }

        // Concept Explainer Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONCEPT RADAR & ACTIVE RECALL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ask Shinobi to explain a concept. He will explain it using real world analogies and test your understanding immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = explanationQuery,
                            onValueChange = { explanationQuery = it },
                            placeholder = { Text("e.g. Recursion, Pointer, Binary Tree...") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("field_concept_query"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShinobiRed,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (explanationQuery.isNotBlank()) {
                                    onExplainConcept(explanationQuery)
                                    explanationQuery = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("btn_concept_explain")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send Icon to explanation")
                        }
                    }
                }
            }
        }

        // Historical study sessions
        if (studySessions.isNotEmpty()) {
            item {
                Text(
                    text = "HISTORICAL MIND TRAINING SESSIONS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(studySessions) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = session.subject, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "${session.actualMinutes}m logged",
                                color = SkyTeal,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Learned: ${session.debriefWhatLearned}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (session.debriefNextSteps.isNotBlank()) {
                            Text(
                                text = "Next step: ${session.debriefNextSteps}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MentorScreen(
    chatHistory: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onGenerateReport: () -> Unit,
    isGenerating: Boolean
) {
    var pendingMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mentor info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = "Mentor Icon", tint = ShinobiRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DOJO CHAT WITH SHINOBI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { onGenerateReport() },
                            modifier = Modifier.testTag("mentor_btn_report")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Weekly report summary trigger", tint = SkyTeal)
                        }
                        IconButton(
                            onClick = { onClearChat() },
                            modifier = Modifier.testTag("mentor_btn_clear")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear chat history logs", tint = TextSecondary)
                        }
                    }
                }
            }
        }

        // Messages Flow
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            reverseLayout = false
        ) {
            items(chatHistory) { msg ->
                val isShinobi = msg.sender == "SHINOBI"
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isShinobi) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isShinobi) MaterialTheme.colorScheme.surfaceVariant else ShinobiRed
                        ),
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = if (isShinobi) 0.dp else 8.dp,
                            bottomEnd = if (isShinobi) 8.dp else 0.dp
                        ),
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .testTag("chat_msg_${msg.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isShinobi) "SHINOBI" else "YOU",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isShinobi) ShinobiRed else Color.White,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .widthIn(max = 180.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = ShinobiRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Shinobi is checking...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chat text entry
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = pendingMessage,
                onValueChange = { pendingMessage = it },
                placeholder = { Text("Formulate your check-in report or query...") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("mentor_chat_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShinobiRed,
                    unfocusedBorderColor = BorderColor
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (pendingMessage.isNotBlank()) {
                        onSendMessage(pendingMessage)
                        pendingMessage = ""
                    }
                },
                enabled = !isGenerating && pendingMessage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("mentor_chat_submit")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message to Shinobi")
            }
        }
    }
}

@Composable
fun CheckInDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Int, Int, Int, Boolean, String) -> Unit
) {
    var yesterdayCompleted by remember { mutableStateOf("") }
    var yesterdaySkips by remember { mutableStateOf("") }
    var energyRating by remember { mutableStateOf(3) }
    var focusRating by remember { mutableStateOf(3) }
    var moodRating by remember { mutableStateOf(3) }
    var honestyChecked by remember { mutableStateOf(false) }
    var selectedSkipReason by remember { mutableStateOf("Avoided hard work") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "DAILY ACCOUNTABILITY MIRROR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ShinobiRed,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Yesterday's deeds are either done or avoided. State them without filter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = yesterdayCompleted,
                    onValueChange = { yesterdayCompleted = it },
                    label = { Text("What did you ACTUALLY complete yesterday?") },
                    placeholder = { Text("Be precise. Do not exaggerate.") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkin_completed_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShinobiRed,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = yesterdaySkips,
                    onValueChange = { yesterdaySkips = it },
                    label = { Text("What did you skip / abandon?") },
                    placeholder = { Text("The things you left on details or avoided...") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkin_skips_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShinobiRed,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Skip reason selector if skips happened
                Text(
                    text = "If something was skipped, what is the honest reason? Stalled routine is documented.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Avoided hard work", "Tired / Lazy", "No enough time", "Distracted by phone", "Physical pain").forEach { reason ->
                        val selected = selectedSkipReason == reason
                        OutlinedButton(
                            onClick = { selectedSkipReason = reason },
                            modifier = Modifier.testTag("skip_reason_$reason"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) ShinobiRed else Color.Transparent
                            ),
                            border = BorderStroke(1.dp, if (selected) ShinobiRed else BorderColor)
                        ) {
                            Text(reason, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sliders
                Text(text = "Physical Energy Rating: $energyRating/5", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = energyRating.toFloat(),
                    onValueChange = { energyRating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.testTag("checkin_slider_energy"),
                    colors = SliderDefaults.colors(thumbColor = ShinobiRed, activeTrackColor = ShinobiRed)
                )

                Text(text = "Study Focus Index: $focusRating/5", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = focusRating.toFloat(),
                    onValueChange = { focusRating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.testTag("checkin_slider_focus"),
                    colors = SliderDefaults.colors(thumbColor = SkyTeal, activeTrackColor = SkyTeal)
                )

                Text(text = "Emotional Mood Indicator: $moodRating/5", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = moodRating.toFloat(),
                    onValueChange = { moodRating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.testTag("checkin_slider_mood"),
                    colors = SliderDefaults.colors(thumbColor = MysticIndigo, activeTrackColor = MysticIndigo)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Honesty Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("checkbox_honesty_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = honestyChecked,
                        onCheckedChange = { honestyChecked = it },
                        modifier = Modifier.testTag("checkbox_honesty")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I am being brutally honest. I understand self-deception only harms my own progression.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalSkip = if (yesterdaySkips.isNotBlank()) selectedSkipReason else ""
                    onSubmit(
                        yesterdayCompleted,
                        yesterdaySkips,
                        energyRating,
                        focusRating,
                        moodRating,
                        honestyChecked,
                        finalSkip
                    )
                },
                enabled = yesterdayCompleted.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                modifier = Modifier.testTag("checkin_submit_btn")
            ) {
                Text("TRANSMIT TO MIRROR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier.testTag("checkin_dismiss_btn")
            ) {
                Text("CANCEL", color = TextSecondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ApiSettingsDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(profile.customApiKey) }
    var enableSearchInput by remember { mutableStateOf(profile.enableSearchGrounding) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "DOJO CREDENTIALS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ShinobiRed,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "If you encounter a 403 Forbidden or compromise error, load your own personal Google Gemini API Key here to bypass restrictions. It is stored securely on your local device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Custom Gemini API Key") },
                    placeholder = { Text("Paste AI Studio Key here...") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_apikey_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShinobiRed,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Don't have a key? Go to google.dev/gemini to get a free personal API key, then paste it above.",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberGold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Toggle for internet search grounding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { enableSearchInput = !enableSearchInput }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Real-time Google Search",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Activate real-time web searches for dynamic, up-to-date physical workout blueprints and deep explanations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = enableSearchInput,
                        onCheckedChange = { enableSearchInput = it },
                        modifier = Modifier.testTag("settings_search_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ShinobiRed,
                            checkedTrackColor = ShinobiRedMuted
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(apiKeyInput, enableSearchInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShinobiRed),
                modifier = Modifier.testTag("settings_save_btn")
            ) {
                Text("OPTIMIZE TRAINING", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("DISMISS", color = TextSecondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

