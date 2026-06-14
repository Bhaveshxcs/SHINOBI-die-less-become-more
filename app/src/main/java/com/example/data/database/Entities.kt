package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val goals: String,
    val weaknesses: String,
    val routineWakeTime: String,
    val routineSleepTime: String,
    val discipline: Int = 1, // Progress indicators (1 to 5)
    val knowledge: Int = 1,
    val body: Int = 1,
    val awareness: Int = 1,
    val activePlan: String = "Self-Improvement Trial",
    val lastCheckInDate: String = "",
    val streakOfHonestyFlags: Int = 0,
    val lastUpdateTimestamp: Long = System.currentTimeMillis(),
    val customApiKey: String = "",
    val enableSearchGrounding: Boolean = true
)

@Entity(tableName = "daily_check_ins")
data class DailyCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // format YYYY-MM-DD
    val yesterdayCompleted: String, // what was done
    val yesterdaySkips: String, // what was skipped
    val energyRating: Int, // 1-5
    val focusRating: Int, // 1-5
    val moodRating: Int, // 1-5
    val honestyFlag: Boolean,
    val skipReason: String, // e.g. "Tired" / "No time" / etc.
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,
    val title: String,
    val exercises: String, // Multiline list of warmup and circuits
    val status: String, // "PLANNED", "COMPLETED", "SKIPPED", "MINIMUM_VIABLE"
    val skipReason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,
    val subject: String,
    val targetGoal: String,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val debriefWhatLearned: String = "",
    val debriefWhatConfused: String = "",
    val debriefNextSteps: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER" or "SHINOBI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
