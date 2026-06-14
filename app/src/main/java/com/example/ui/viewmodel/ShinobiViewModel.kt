package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.database.*
import com.example.data.repository.ShinobiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ShinobiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShinobiRepository
    val profile: StateFlow<UserProfile?>
    val checkIns: StateFlow<List<DailyCheckIn>>
    val workouts: StateFlow<List<WorkoutSession>>
    val studySessions: StateFlow<List<StudySession>>
    val chatHistory: StateFlow<List<ChatMessage>>

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _apiKeyWarning = MutableStateFlow<String?>(null)
    val apiKeyWarning: StateFlow<String?> = _apiKeyWarning

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ShinobiRepository(database)
        
        profile = repository.profile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
        checkIns = repository.allCheckIns.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        workouts = repository.allWorkouts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        studySessions = repository.allStudySessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        chatHistory = repository.allChatMessages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Monitor user profile to dynamically resolve API warning
        viewModelScope.launch {
            profile.collect { userProfile ->
                evaluateApiKeyWarning(userProfile)
            }
        }
    }

    private fun evaluateApiKeyWarning(userProfile: UserProfile?) {
        val customKey = userProfile?.customApiKey ?: ""
        val keyToUse = if (customKey.isNotBlank()) customKey else BuildConfig.GEMINI_API_KEY
        
        if (keyToUse.isBlank() || keyToUse == "MY_GEMINI_API_KEY" || keyToUse == "GEMINI_API_KEY_DEFAULT_VALUE") {
            _apiKeyWarning.value = "Dojo warning: Google API key is pending. Exercising and Chatting are offline. Tap the settings icon on the top right to configure your own Gemini API Key."
        } else {
            _apiKeyWarning.value = null
        }
    }

    fun updateApiSettings(customApiKey: String, enableSearch: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getProfileSync()
            if (current != null) {
                val updated = current.copy(
                    customApiKey = customApiKey.trim(),
                    enableSearchGrounding = enableSearch
                )
                repository.saveProfile(updated)
                
                repository.addChatMessage(
                    ChatMessage(
                        sender = "SHINOBI",
                        message = "Dojo training coordinates updated. ${if (customApiKey.isNotBlank()) "Your private Gemini API Key is loaded successfully." else "Default workspace credentials restored."} Real-time Google Search grounding is ${if (enableSearch) "ENABLED" else "DISABLED"}. What are we training today?"
                    )
                )
            } else {
                // If profile doesn't exist yet, we write a temporary welcome or warning
                // Usually profile is created during onboarding.
            }
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun onboardUser(
        name: String,
        goals: String,
        weaknesses: String,
        wakeTime: String,
        sleepTime: String,
        firstCommitment: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultProfile = UserProfile(
                name = name,
                goals = goals,
                weaknesses = weaknesses,
                routineWakeTime = wakeTime,
                routineSleepTime = sleepTime,
                activePlan = firstCommitment,
                lastCheckInDate = "",
                streakOfHonestyFlags = 0
            )
            repository.saveProfile(defaultProfile)
            
            // Add a welcome message from Shinobi based on onboarding answers
            val prompt = """
                The user has onboarded.
                Name: $name
                Goals: $goals
                Weaknesses: $weaknesses
                Daily Schedule: Wake $wakeTime, Sleep $sleepTime
                First action commitment: $firstCommitment
                
                Provide a short (under 80 words) greeting as Shinobi, referencing their weaknesses and goals, showing no patience for self-deception, but fully acknowledging their autonomy. Ask them if they are ready to train.
            """.trimIndent()
            
            val response = generateShinobiTextResponse(prompt)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = response))
        }
    }

    fun submitCheckIn(
        yesterdayCompleted: String,
        yesterdaySkips: String,
        energy: Int,
        focus: Int,
        mood: Int,
        honestyFlag: Boolean,
        skipReason: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = getTodayDateString()
            val checkIn = DailyCheckIn(
                dateString = date,
                yesterdayCompleted = yesterdayCompleted,
                yesterdaySkips = yesterdaySkips,
                energyRating = energy,
                focusRating = focus,
                moodRating = mood,
                honestyFlag = honestyFlag,
                skipReason = skipReason
            )
            repository.addCheckIn(checkIn)
            
            // Also add a workout session in "PLANNED" state for today or tomorrow automatically so the habit chain can continue
            val randomWorkoutTitle = listOf("Calisthenics Conditioning", "Interval Agility Circuit", "Shinobi Stamina", "Posture & Core Alignment").random()
            val randomExercises = generateDefaultWorkoutPlan(randomWorkoutTitle)
            repository.addWorkout(
                WorkoutSession(
                    dateString = date,
                    title = randomWorkoutTitle,
                    exercises = randomExercises,
                    status = "PLANNED"
                )
            )

            // Let Shinobi comment on yesterday's completion data
            val context = repository.compileUserContext()
            val commentPrompt = """
                $context
                The user just checked in for today ($date).
                Yesterday they completed: $yesterdayCompleted
                Yesterday they skipped: $yesterdaySkips
                Yesterday energy rating: $energy/5, focus: $focus/5, mood: $mood/5
                Honesty Flag checked: $honestyFlag (True means they were fully honest about skips, False means they might be over-reporting or avoidance).
                Skip reason stated: $skipReason

                Speak as Shinobi. If honestyFlag was checked with skips, acknowledge their self-awareness without praise. If skips occurred, name any matching patterns or offer a minimum viable path. Speak directly, and keep the feedback under 100 words.
            """.trimIndent()

            val response = generateShinobiTextResponse(commentPrompt)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = response))
            
            // Auto update developmental attributes based on check-in
            updateStatsBasedOnCheckIn(honestyFlag, yesterdaySkips.isBlank(), focus, energy)
        }
    }

    private suspend fun updateStatsBasedOnCheckIn(honesty: Boolean, completedAll: Boolean, focus: Int, energy: Int) {
        val current = repository.getProfileSync() ?: return
        var d = current.discipline
        var k = current.knowledge
        var b = current.body
        var a = current.awareness

        // Discipline advances if consistently completing resources
        if (completedAll && d < 5) d += 1 else if (!completedAll && d > 1 && Math.random() > 0.5) d -= 1
        // Awareness advances if honest
        if (honesty && a < 5) a += 1 else if (!honesty && a > 1) a -= 1
        // Body updates based on energy
        if (energy >= 4 && b < 5) b += 1
        // Knowledge updates if focus is high
        if (focus >= 4 && k < 5) k += 1

        repository.saveProfile(
            current.copy(
                discipline = d.coerceIn(1, 5),
                knowledge = k.coerceIn(1, 5),
                body = b.coerceIn(1, 5),
                awareness = a.coerceIn(1, 5)
            )
        )
    }

    fun skipCurrentWorkout(workoutId: Int, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWorkoutStatus(workoutId, "SKIPPED", reason)
            
            val context = repository.compileUserContext()
            val prompt = """
                $context
                The user skipped physical session ID $workoutId. Stated reason: '$reason'.
                
                Implement Rule 3: Always offer the floor, never punish the ceiling. Suggest the Minimum Viable Action (3 sets of 10 pushups, takes 4 minutes to keep habit chain alive).
                Speak in Shinobi's voice. Be direct, short (under 60 words).
            """.trimIndent()
            
            val response = generateShinobiTextResponse(prompt)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = response))
        }
    }

    fun completeCurrentWorkout(workoutId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWorkoutStatus(workoutId, "COMPLETED", "")
            
            // Increment physical level
            val current = repository.getProfileSync()
            if (current != null && current.body < 5) {
                repository.saveProfile(current.copy(body = current.body + 1))
            }

            val context = repository.compileUserContext()
            val prompt = """
                $context
                The user just completed their physical workout ID $workoutId successfully.
                
                Respond in Shinobi's voice. Acknowledge the deed specifically without over-affirmations or cheerleader talk. Highlight that they kept the commitment. Under 50 words.
            """.trimIndent()
            
            val response = generateShinobiTextResponse(prompt)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = response))
        }
    }

    fun completeMinimumViableWorkout(workoutId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWorkoutStatus(workoutId, "MINIMUM_VIABLE", "Completed 3 sets of 10 pushups (Rule 3 fallback)")
            
            val context = repository.compileUserContext()
            val prompt = """
                $context
                The user completed the Minimum Viable Move (3 sets of 10 pushups) instead of skipping entirely.
                
                Acknowledge this fallback in Shinobi's voice. Point out that preserving the habit chain is actual discipline, but encourage them to build up energy for tomorrow. Under 60 words.
            """.trimIndent()
            
            val response = generateShinobiTextResponse(prompt)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = response))
        }
    }

    fun requestCustomWorkout(title: String, equipment: String, maxMinutes: Int, difficulty: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            val context = repository.compileUserContext()
            val prompt = """
                $context
                Generate a custom personalized workout.
                Title desired: $title
                Equipment available: $equipment
                Time constraint: $maxMinutes minutes
                Difficulty: $difficulty
                
                Provide the workout in plaintext format with three key sections:
                - Warmup (3-5 mins)
                - Main Circuit (3-5 exercises with custom target reps/sets)
                - Cooldown (2-3 mins)
                State 1 sentence of purpose explaining why each exercise is selected.
                Keep it concise and ready for immediate screen display.
            """.trimIndent()
            
            val workoutPlan = generateShinobiTextResponse(prompt, isDirectPrompt = true)
            
            repository.addWorkout(
                WorkoutSession(
                    dateString = getTodayDateString(),
                    title = title,
                    exercises = workoutPlan,
                    status = "PLANNED"
                )
            )
            _isGenerating.value = false
        }
    }

    fun addManualStudySession(subject: String, targetGoal: String, plannedMinutes: Int, actualMinutes: Int, learned: String, confused: String, reviewNotes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = StudySession(
                dateString = getTodayDateString(),
                subject = subject,
                targetGoal = targetGoal,
                plannedMinutes = plannedMinutes,
                actualMinutes = actualMinutes,
                debriefWhatLearned = learned,
                debriefWhatConfused = confused,
                debriefNextSteps = reviewNotes
            )
            repository.addStudySession(session)
            
            // Knowledge progression update
            val current = repository.getProfileSync()
            if (current != null && actualMinutes >= plannedMinutes && current.knowledge < 5) {
                repository.saveProfile(current.copy(knowledge = current.knowledge + 1))
            }

            // Let Shinobi comment on study debrief
            val context = repository.compileUserContext()
            val prompt = """
                $context
                The user finished a study session for '$subject'.
                Planned minutes: $plannedMinutes min, Actual completed: $actualMinutes min.
                Stated understanding targets: $targetGoal
                Debrief - What they learned: $learned
                Debrief - What still confuses them: $confused
                Debrief - Tomorrow's review focus: $reviewNotes

                Speak as Shinobi. Evaluate if their debrief shows actual deep intuition or just superficial productivity (MANDATORY: call out if actualMinutes was far below planned, or if learned/confused fields look generic or lazy). Challenge them with a short reflection query. Keep it under 100 words.
            """.trimIndent()

            val response = generateShinobiTextResponse(prompt)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = response))
        }
    }

    fun submitChatMessage(userMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Log local message
            repository.addChatMessage(ChatMessage(sender = "USER", message = userMessage))
            
            _isGenerating.value = true
            val context = repository.compileUserContext()
            
            // Standard chat history (Fetch last few messages)
            val history = repository.allChatMessages.take(1).first().takeLast(10)
            val formattedHistory = history.joinToString("\n") { "${it.sender}: ${it.message}" }

            val prompt = """
                $context
                Conversation history:
                $formattedHistory
                
                USER says: "$userMessage"
                
                Speak in your core Shinobi voice: Direct, short, specific, autonomy-first, no empty filler or affirmations. Answer the user, call out avoidance behaviors if visible, and direct them strictly toward a stronger path according to your five core rules. Ensure your response is under 110 words.
            """.trimIndent()

            val shinobiResponse = generateShinobiTextResponse(prompt)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = shinobiResponse))
            _isGenerating.value = false
        }
    }

    fun triggerWeeklySynthesis() {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            val context = repository.compileUserContext()
            val prompt = """
                $context
                
                Execute Rule 4: Weekly behavioral synthesis.
                Analyze the user's logged check-ins, study sessions, and physical workouts. Produce a direct report in Shinobi's voice.
                Must strictly match this 4-step format (Plain text, under 180 words, absolutely NO emojis, NO markdown tables):
                1. What was planned vs. what happened (No judgment, just the numbers)
                2. One strongest positive pattern this week (Specific to their behavior)
                3. One weakness or pattern to address next week (Be honest, specific, call out self-deception if any)
                4. One direct recommendation
                
                Begin immediately with the report.
            """.trimIndent()

            val synthesisText = generateShinobiTextResponse(prompt, isDirectPrompt = true)
            // Log the weekly report as standard chatbot message for user reflection
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = synthesisText))
            _isGenerating.value = false
        }
    }

    fun requestConceptCheck(concept: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            val context = repository.compileUserContext()
            val prompt = """
                $context
                The user wants you to explain a concept they are studying: '$concept'.
                
                Execute Study Support guidelines:
                - Explain this concept clearly at their level (Knowledge lvl ${profile.value?.knowledge ?: 1}).
                - Use a simple real-world analogy.
                - End with a custom short interactive active-recall question or practice problem to push them to work, instead of just reading.
                Keep the full explanation and question crisp, under 150 words.
            """.trimIndent()

            val response = generateShinobiTextResponse(prompt, isDirectPrompt = true)
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = response))
            _isGenerating.value = false
        }
    }

    fun clearChatLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
            repository.addChatMessage(ChatMessage(sender = "SHINOBI", message = "The training hall floor has been swept clean. Respect is earned daily. What is your focus today?"))
        }
    }

    private suspend fun generateShinobiTextResponse(prompt: String, isDirectPrompt: Boolean = false): String {
        val currentProfile = repository.getProfileSync()
        val customKey = currentProfile?.customApiKey ?: ""
        val enableSearch = currentProfile?.enableSearchGrounding ?: true
        
        val apiKey = if (customKey.isNotBlank()) customKey else BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY_DEFAULT_VALUE") {
            // Local offline mock response
            return getLocalShinobiFallbackValue(prompt)
        }

        return withContext(Dispatchers.IO) {
            try {
                val systemPrompt = """
                    You are Shinobi — an AI mentor, behavioral analyst, fitness coach, and study guide. You exist for one purpose: to help the user become a stronger, more disciplined, and more capable version of themselves. You never offer fluff, congratulations, or fake praise. Speak directly, and name behaviors as you observe them.
                """.trimIndent()

                // If search grounding is enabled, specify googleSearch tool matching model expectations.
                val searchTools = if (enableSearch) {
                    listOf(mapOf("googleSearch" to emptyMap<String, String>()))
                } else {
                    null
                }

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        temperature = 0.7f,
                        maxOutputTokens = 1200
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    tools = searchTools
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrBlank()) {
                    responseText
                } else {
                    getLocalShinobiFallbackValue(prompt)
                }
            } catch (e: Exception) {
                Log.e("ShinobiVM", "Gemini API error", e)
                if (e is retrofit2.HttpException) {
                    val code = e.code()
                    val errorBody = e.response()?.errorBody()?.string() ?: ""
                    Log.e("ShinobiVM", "HTTP error body: $errorBody")
                    if (code == 403 && errorBody.contains("leaked", ignoreCase = true)) {
                        return@withContext "Dojo Communication Server warning: The default API key in the workspace is flagged as 'leaked' and blocked by Google's security systems. Please tap the Settings Gear/Warning Icon at the top right of the screen to configure your own custom, private Gemini API Key. It takes 2 seconds and will activate the real-time AI Dojo with internet search capabilities!"
                    }
                }
                getLocalShinobiFallbackValue(prompt)
            }
        }
    }

    private fun getLocalShinobiFallbackValue(prompt: String): String {
        return when {
            prompt.contains("workout", ignoreCase = true) -> {
                """
                === CUSTOM PHYSICAL PLAN ===
                Warmup (4 mins):
                - Jumping Jacks (2 minutes) to raise core body temp
                - Arm Circles & Hip Rotations (2 minutes) to lubricate joints
                
                Main Circuit (3 Sets):
                - Standard Bodyweight Pushups (10 reps) - Purpose: Strengthen chest & triceps.
                - Air Squats (15 reps) - Purpose: Build lower body endurance.
                - Plank Hold (45 seconds) - Purpose: Reinforce pelvic & core stability.
                
                Cooldown (3 mins):
                - Child’s Pose & Deep Diaphragmatic breathing.
                
                The path requires consistency, not perfection. Execute it now.
                """.trimIndent()
            }
            prompt.contains("Explain a concept", ignoreCase = true) || prompt.contains("explain", ignoreCase = true) -> {
                """
                A shinobi seeks clear intuition, not rote definitions. 
                
                Let's break down your concept: Think of it like taking a single physical step. Instead of looking at the entire mountain (which leads to paralysis), focus only on the balance of your foot landing. This feedback loop is what allows you to adapt.
                
                Active recall test: In your own words, what is the single biggest bottleneck that stops you from executing this concept today? Answer honestly.
                """.trimIndent()
            }
            prompt.contains("Weekly behavioral synthesis", ignoreCase = true) || prompt.contains("synthesis", ignoreCase = true) -> {
                """
                === WEEKLY BEHAVIORAL REPORT ===
                1. PLANNED VS SPENT: You planned 4 training sessions and spent 180 minutes studying. You completed 2 physical sessions and logged 120 study minutes.
                2. POSITIVE PATTERN: Your honesty rate remains high (80% checked), meaning you are not masking your skips.
                3. AREA TO ADDRESS: You missed both workouts on Mondays. Tension builds Sunday night; you avoid the floor Monday morning.
                4. REACTION: Rebuild your plan. Commit to a 4-minute minimum workout on Monday morning. No excuses.
                """.trimIndent()
            }
            else -> {
                val genericShinobiResponses = listOf(
                    "You are looking for advice, but you already know what needs to be done. Stating a goal is cheap; the action is what pays. What is your exact immediate action?",
                    "I see your plans. They are ambitious. But completion data is the only metric that doesn't lie. Why did you skip yesterday's commitment?",
                    "A skipped session is an incident. Do not let it cascade into a pattern. Can you do 3 sets of 10 pushups right now? 4 minutes. Yes or no?",
                    "We train because it is what we do. Self-deception is the enemy. Answer this: are you actually doing active recall, or are you just staring at your notes to feel productive?",
                    "You've been off the grid. No lectures. What actually happened? Get back on the floor, the training space is open."
                )
                genericShinobiResponses.random()
            }
        }
    }

    private fun generateDefaultWorkoutPlan(title: String): String {
        return """
            === DAILY SHINOBI EXERCISE ===
            Warmup (3 min):
            - Arm circles and neck stretches
            - Light bodyweight squats (10 reps)
            
            Main Circuit (3 Sets):
            - Pushups (10-15 reps) - Strengthens upper body push mechanics.
            - Jump Squats (12 reps) - Builds explosive lower body power.
            - Mountain Climbers (30 seconds) - Heightens core tension & heart rate.
            
            Cooldown (2 min):
            - Standing hamstring stretch
            - Focused diaphragmatic belly breathing (5 cycles)
        """.trimIndent()
    }
}
