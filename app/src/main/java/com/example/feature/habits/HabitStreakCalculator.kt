package com.example.feature.habits

import com.example.data.Habit
import com.example.data.HabitLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitItemUiState(
    val habit: Habit,
    val isDoneToday: Boolean,
    val currentStreak: Int,
    val todayLog: HabitLog? = null
)

/**
 * Calculates current streak backward from today.
 * If today is completed, count includes today and checks backwards (yesterday, day before...).
 * If today is NOT completed yet, streak checks backwards starting from yesterday
 * (so a user who completed yesterday hasn't broken their streak yet today).
 * Stops at the first day without completed = true.
 */
fun computeStreak(logs: List<HabitLog>, today: LocalDate = LocalDate.now()): Int {
    val completedDates = logs
        .filter { it.completed }
        .map { it.date }
        .toSet()

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE // "YYYY-MM-DD"
    val todayStr = today.format(formatter)
    val isDoneToday = completedDates.contains(todayStr)

    var streak = 0
    var checkDate = if (isDoneToday) today else today.minusDays(1)

    while (true) {
        val checkStr = checkDate.format(formatter)
        if (completedDates.contains(checkStr)) {
            streak++
            checkDate = checkDate.minusDays(1)
        } else {
            break
        }
    }

    return streak
}

fun getTodayDateString(today: LocalDate = LocalDate.now()): String {
    return today.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
