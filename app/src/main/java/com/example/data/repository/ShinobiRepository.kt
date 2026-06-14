package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class ShinobiRepository(private val db: AppDatabase) {
    private val userProfileDao = db.userProfileDao()
    private val dailyCheckInDao = db.dailyCheckInDao()
    private val workoutSessionDao = db.workoutSessionDao()
    private val studySessionDao = db.studySessionDao()
    private val chatMessageDao = db.chatMessageDao()

    val profile: Flow<UserProfile?> = userProfileDao.getProfile()
    val allCheckIns: Flow<List<DailyCheckIn>> = dailyCheckInDao.getAllCheckIns()
    val allWorkouts: Flow<List<WorkoutSession>> = workoutSessionDao.getAllWorkouts()
    val allStudySessions: Flow<List<StudySession>> = studySessionDao.getAllStudySessions()
    val allChatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun getProfileSync(): UserProfile? = userProfileDao.getProfileSync()

    suspend fun saveProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdate(profile)
    }

    suspend fun addCheckIn(checkIn: DailyCheckIn) {
        dailyCheckInDao.insert(checkIn)
        
        // Update profile last check in date and evaluate honesty flag streak
        val currentProfile = getProfileSync()
        if (currentProfile != null) {
            val newHonestyStreak = if (checkIn.honestyFlag) {
                currentProfile.streakOfHonestyFlags + 1
            } else {
                0
            }
            // Update the profile with new check-in date and streak
            saveProfile(
                currentProfile.copy(
                    lastCheckInDate = checkIn.dateString,
                    streakOfHonestyFlags = newHonestyStreak
                )
            )
        }
    }

    suspend fun addWorkout(workout: WorkoutSession) {
        workoutSessionDao.insert(workout)
    }

    suspend fun updateWorkoutStatus(id: Int, status: String, skipReason: String) {
        workoutSessionDao.updateStatus(id, status, skipReason)
    }

    suspend fun addStudySession(studySession: StudySession) {
        studySessionDao.insert(studySession)
    }

    suspend fun addChatMessage(message: ChatMessage) {
        chatMessageDao.insert(message)
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
    }

    // Context Generation
    suspend fun compileUserContext(): String {
        val currentProfile = getProfileSync() ?: return "[NO USER CONTEXT]"
        
        val recentCheckIns = dailyCheckInDao.getRecentCheckInsSync()
        val recentWorkouts = workoutSessionDao.getRecentWorkoutsSync()
        val recentStudySessions = studySessionDao.getRecentStudySessionsSync()

        // Analyze behavior for the last 14 days summary
        val studySessionsPlanned = recentStudySessions.size
        val studySessionsMinutes = recentStudySessions.sumOf { it.actualMinutes }
        
        val workoutCompletedCount = recentWorkouts.count { it.status == "COMPLETED" || it.status == "MINIMUM_VIABLE" }
        val workoutSkippedCount = recentWorkouts.count { it.status == "SKIPPED" }
        
        val totalCheckIns = recentCheckIns.size
        val avgEnergy = if (recentCheckIns.isNotEmpty()) recentCheckIns.map { it.energyRating }.average() else 3.0
        val avgFocus = if (recentCheckIns.isNotEmpty()) recentCheckIns.map { it.focusRating }.average() else 3.0
        val avgMood = if (recentCheckIns.isNotEmpty()) recentCheckIns.map { it.moodRating }.average() else 3.0
        
        val behavioralSummary = StringBuilder()
        behavioralSummary.append("Over the last $totalCheckIns active days: ")
        behavioralSummary.append("Average physical energy rating is ${String.format("%.1f", avgEnergy)}/5. ")
        behavioralSummary.append("Study session focus index is ${String.format("%.1f", avgFocus)}/5. ")
        behavioralSummary.append("Mood index is ${String.format("%.1f", avgMood)}/5. ")
        behavioralSummary.append("Logged $workoutCompletedCount completed/minimum physical workouts, skipped $workoutSkippedCount. ")
        behavioralSummary.append("Studied $studySessionsPlanned times for a total of $studySessionsMinutes minutes.")

        val skipReasons = recentCheckIns.mapNotNull { it.skipReason.ifBlank { null } }
            .plus(recentWorkouts.mapNotNull { it.skipReason.ifBlank { null } })
            .distinct()
            .joinToString(", ")
            .ifBlank { "None documented" }

        return """
            [USER_CONTEXT]
            Name: ${currentProfile.name}
            Goals: ${currentProfile.goals}
            Identified weaknesses: ${currentProfile.weaknesses}
            Current progression — Discipline: ${currentProfile.discipline}, Knowledge: ${currentProfile.knowledge}, Body: ${currentProfile.body}, Awareness: ${currentProfile.awareness}
            Active plan: ${currentProfile.activePlan}
            Last 14 days summary: $behavioralSummary
            Recent skip reasons: $skipReasons
            Current streak of honesty flags: ${currentProfile.streakOfHonestyFlags}
        """.trimIndent()
    }
}
