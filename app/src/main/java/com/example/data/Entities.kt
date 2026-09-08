package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// --- 1. Hackathon Entry ---
enum class HackathonStatus {
    REGISTERED,
    IN_PROGRESS,
    SUBMITTED,
    MISSED
}

@Entity(tableName = "hackathons")
data class HackathonEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val status: HackathonStatus,
    val registrationDate: Long, // Epoch timestamp in milliseconds
    val deadlineDate: Long,     // Epoch timestamp in milliseconds
    val projectLink: String? = null,
    val learnings: String? = null
)

// --- 2. Habit ---
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdDate: Long,      // Epoch timestamp in milliseconds
    val isActive: Boolean = true
)

// --- 3. Habit Log ---
@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "date"], unique = true)
    ]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,          // ISO date format "YYYY-MM-DD"
    val completed: Boolean
)

// --- 4. Focus Session ---
enum class FocusCategory {
    STUDY,
    WORKOUT,
    CUSTOM
}

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: FocusCategory,
    val durationMinutes: Int,
    val timestamp: Long,       // Epoch timestamp in milliseconds
    val note: String? = null
)

// --- 5. DSA Problem ---
enum class DsaPlatform {
    LEETCODE,
    CODEFORCES,
    GFG,
    OTHER
}

enum class DsaDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class DsaStatus {
    SOLVED,
    ATTEMPTED,
    TO_DO
}

@Entity(tableName = "dsa_problems")
data class DsaProblem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val platform: DsaPlatform,
    val difficulty: DsaDifficulty,
    val topic: String,
    val status: DsaStatus,
    val dateSolved: Long? = null // Epoch timestamp in milliseconds
)

// --- 6. Project ---
enum class ProjectStatus {
    IDEA,
    IN_PROGRESS,
    COMPLETED
}

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val techStack: String,
    val status: ProjectStatus,
    val repoLink: String? = null
)
