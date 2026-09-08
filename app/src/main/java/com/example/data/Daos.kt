package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// --- 1. Hackathon DAO ---
@Dao
interface HackathonDao {
    @Query("SELECT * FROM hackathons ORDER BY deadlineDate ASC")
    fun getAllHackathons(): Flow<List<HackathonEntry>>

    @Query("SELECT * FROM hackathons WHERE id = :id")
    fun getHackathonById(id: Long): Flow<HackathonEntry?>

    @Query("SELECT * FROM hackathons WHERE deadlineDate >= :currentTime AND deadlineDate <= :warningThresholdTime AND status != 'SUBMITTED' ORDER BY deadlineDate ASC")
    fun getUpcomingWarningHackathons(currentTime: Long, warningThresholdTime: Long): Flow<List<HackathonEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHackathon(entry: HackathonEntry): Long

    @Update
    suspend fun updateHackathon(entry: HackathonEntry)

    @Delete
    suspend fun deleteHackathon(entry: HackathonEntry)

    @Query("DELETE FROM hackathons WHERE id = :id")
    suspend fun deleteHackathonById(id: Long)
}

// --- 2. Habit DAO ---
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdDate DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdDate DESC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: Long): Flow<Habit?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)
}

// --- 3. Habit Log DAO ---
@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs")
    fun getAllLogs(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getLogsListForHabit(habitId: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLog(habitId: Long, date: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun getLogFlow(habitId: Long, date: String): Flow<HabitLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLog): Long

    @Delete
    suspend fun deleteLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}

// --- 4. Focus Session DAO ---
@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE category = :category ORDER BY timestamp DESC")
    fun getSessionsByCategory(category: FocusCategory): Flow<List<FocusSession>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalMinutesBetween(startTime: Long, endTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    @Delete
    suspend fun deleteSession(session: FocusSession)

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}

// --- 5. DSA Problem DAO ---
@Dao
interface DsaProblemDao {
    @Query("SELECT * FROM dsa_problems ORDER BY id DESC")
    fun getAllProblems(): Flow<List<DsaProblem>>

    @Query("SELECT * FROM dsa_problems WHERE status = :status ORDER BY id DESC")
    fun getProblemsByStatus(status: DsaStatus): Flow<List<DsaProblem>>

    @Query("SELECT * FROM dsa_problems WHERE platform = :platform ORDER BY id DESC")
    fun getProblemsByPlatform(platform: DsaPlatform): Flow<List<DsaProblem>>

    @Query("SELECT * FROM dsa_problems WHERE difficulty = :difficulty ORDER BY id DESC")
    fun getProblemsByDifficulty(difficulty: DsaDifficulty): Flow<List<DsaProblem>>

    @Query("SELECT COUNT(*) FROM dsa_problems WHERE status = 'SOLVED'")
    fun getSolvedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dsa_problems WHERE status = 'SOLVED' AND dateSolved >= :startTime AND dateSolved <= :endTime")
    fun getSolvedCountBetween(startTime: Long, endTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: DsaProblem): Long

    @Update
    suspend fun updateProblem(problem: DsaProblem)

    @Delete
    suspend fun deleteProblem(problem: DsaProblem)

    @Query("DELETE FROM dsa_problems WHERE id = :id")
    suspend fun deleteProblemById(id: Long)
}

// --- 6. Project DAO ---
@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE status = :status ORDER BY id DESC")
    fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}
