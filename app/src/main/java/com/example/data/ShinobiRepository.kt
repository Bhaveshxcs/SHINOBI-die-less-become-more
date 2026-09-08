package com.example.data

import kotlinx.coroutines.flow.Flow

class ShinobiRepository(private val database: ShinobiDatabase) {

    // --- Hackathons ---
    val allHackathons: Flow<List<HackathonEntry>> = database.hackathonDao().getAllHackathons()

    fun getHackathonById(id: Long): Flow<HackathonEntry?> = database.hackathonDao().getHackathonById(id)

    fun getUpcomingWarningHackathons(currentTime: Long, warningThresholdTime: Long): Flow<List<HackathonEntry>> =
        database.hackathonDao().getUpcomingWarningHackathons(currentTime, warningThresholdTime)

    suspend fun insertHackathon(entry: HackathonEntry): Long = database.hackathonDao().insertHackathon(entry)

    suspend fun updateHackathon(entry: HackathonEntry) = database.hackathonDao().updateHackathon(entry)

    suspend fun deleteHackathon(entry: HackathonEntry) = database.hackathonDao().deleteHackathon(entry)

    suspend fun deleteHackathonById(id: Long) = database.hackathonDao().deleteHackathonById(id)

    // --- Habits ---
    val allHabits: Flow<List<Habit>> = database.habitDao().getAllHabits()
    val activeHabits: Flow<List<Habit>> = database.habitDao().getActiveHabits()

    fun getHabitById(id: Long): Flow<Habit?> = database.habitDao().getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = database.habitDao().insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = database.habitDao().updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = database.habitDao().deleteHabit(habit)

    suspend fun deleteHabitById(id: Long) = database.habitDao().deleteHabitById(id)

    // --- Habit Logs ---
    val allHabitLogs: Flow<List<HabitLog>> = database.habitLogDao().getAllLogs()

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> = database.habitLogDao().getLogsForHabit(habitId)

    suspend fun getLogsListForHabit(habitId: Long): List<HabitLog> = database.habitLogDao().getLogsListForHabit(habitId)

    fun getLogsForDate(date: String): Flow<List<HabitLog>> = database.habitLogDao().getLogsForDate(date)

    suspend fun getHabitLog(habitId: Long, date: String): HabitLog? = database.habitLogDao().getLog(habitId, date)

    fun getHabitLogFlow(habitId: Long, date: String): Flow<HabitLog?> = database.habitLogDao().getLogFlow(habitId, date)

    suspend fun saveHabitLog(log: HabitLog): Long = database.habitLogDao().insertOrUpdateLog(log)

    suspend fun deleteHabitLog(log: HabitLog) = database.habitLogDao().deleteLog(log)

    // --- Focus Sessions ---
    val allFocusSessions: Flow<List<FocusSession>> = database.focusSessionDao().getAllSessions()

    fun getFocusSessionsBetween(start: Long, end: Long): Flow<List<FocusSession>> =
        database.focusSessionDao().getSessionsBetween(start, end)

    fun getFocusSessionsByCategory(category: FocusCategory): Flow<List<FocusSession>> =
        database.focusSessionDao().getSessionsByCategory(category)

    fun getTotalFocusMinutesBetween(start: Long, end: Long): Flow<Int> =
        database.focusSessionDao().getTotalMinutesBetween(start, end)

    suspend fun insertFocusSession(session: FocusSession): Long = database.focusSessionDao().insertSession(session)

    suspend fun deleteFocusSession(session: FocusSession) = database.focusSessionDao().deleteSession(session)

    // --- DSA Problems ---
    val allDsaProblems: Flow<List<DsaProblem>> = database.dsaProblemDao().getAllProblems()

    fun getDsaProblemsByStatus(status: DsaStatus): Flow<List<DsaProblem>> =
        database.dsaProblemDao().getProblemsByStatus(status)

    fun getDsaProblemsByPlatform(platform: DsaPlatform): Flow<List<DsaProblem>> =
        database.dsaProblemDao().getProblemsByPlatform(platform)

    fun getDsaProblemsByDifficulty(difficulty: DsaDifficulty): Flow<List<DsaProblem>> =
        database.dsaProblemDao().getProblemsByDifficulty(difficulty)

    val solvedDsaCount: Flow<Int> = database.dsaProblemDao().getSolvedCount()

    fun getSolvedDsaCountBetween(start: Long, end: Long): Flow<Int> =
        database.dsaProblemDao().getSolvedCountBetween(start, end)

    suspend fun insertDsaProblem(problem: DsaProblem): Long = database.dsaProblemDao().insertProblem(problem)

    suspend fun updateDsaProblem(problem: DsaProblem) = database.dsaProblemDao().updateProblem(problem)

    suspend fun deleteDsaProblem(problem: DsaProblem) = database.dsaProblemDao().deleteProblem(problem)

    suspend fun deleteDsaProblemById(id: Long) = database.dsaProblemDao().deleteProblemById(id)

    // --- Projects ---
    val allProjects: Flow<List<Project>> = database.projectDao().getAllProjects()

    fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>> =
        database.projectDao().getProjectsByStatus(status)

    fun getProjectById(id: Long): Flow<Project?> = database.projectDao().getProjectById(id)

    suspend fun insertProject(project: Project): Long = database.projectDao().insertProject(project)

    suspend fun updateProject(project: Project) = database.projectDao().updateProject(project)

    suspend fun deleteProject(project: Project) = database.projectDao().deleteProject(project)

    suspend fun deleteProjectById(id: Long) = database.projectDao().deleteProjectById(id)
}
