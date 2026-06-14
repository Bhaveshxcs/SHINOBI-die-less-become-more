package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)
}

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_check_ins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckIn>>

    @Query("SELECT * FROM daily_check_ins ORDER BY timestamp DESC LIMIT 14")
    suspend fun getRecentCheckInsSync(): List<DailyCheckIn>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: DailyCheckIn)
}

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC LIMIT 3")
    suspend fun getRecentWorkoutsSync(): List<WorkoutSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutSession)

    @Query("UPDATE workout_sessions SET status = :status, skipReason = :skipReason WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, skipReason: String)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC LIMIT 5")
    suspend fun getRecentStudySessionsSync(): List<StudySession>

    @Query("SELECT * FROM study_sessions WHERE subject = :subject ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSessionForSubject(subject: String): StudySession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studySession: StudySession)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC LIMIT 50")
    suspend fun getRecentMessagesSync(): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}
