package com.example.habits

import com.example.habits.data.model.DayPeriod
import com.example.habits.data.model.Habit
import com.example.habits.data.model.HabitFrequency
import com.example.habits.data.model.HabitStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class HabitTest {
    private val today = LocalDate.of(2026, 11, 6) // sexta-feira
    private val createdAt = LocalDate.of(2026, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun habit(vararg dates: String, frequency: HabitFrequency = HabitFrequency.DAILY, days: Set<DayOfWeek> = DayOfWeek.entries.toSet()) =
        Habit(name = "Beber água", createdAt = createdAt, frequency = frequency, days = days, completedDates = dates.toSet())

    @Test
    fun toggle_marksAndUnmarksDay() {
        val done = habit().toggle(today)
        assertTrue(done.isDoneOn(today))
        assertFalse(done.toggle(today).isDoneOn(today))
    }

    @Test
    fun streak_countsConsecutiveDaysEndingToday() {
        assertEquals(3, habit("2026-11-04", "2026-11-05", "2026-11-06", "2026-11-01").currentStreak(today))
    }

    @Test
    fun streak_keepsYesterdaysSequenceWhileTodayIsPending() {
        assertEquals(2, habit("2026-11-04", "2026-11-05").currentStreak(today))
    }

    @Test
    fun streak_isZeroWhenAScheduledDayWasMissed() {
        assertEquals(0, habit("2026-11-03").currentStreak(today))
    }

    @Test
    fun streak_ignoresDaysOutsideTheSchedule() {
        // Seg, qua e sex: terça e quinta não quebram a sequência.
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val h = habit("2026-11-02", "2026-11-04", "2026-11-06", frequency = HabitFrequency.CUSTOM, days = days)
        assertEquals(3, h.currentStreak(today))
        assertFalse(h.isScheduledOn(LocalDate.of(2026, 11, 5)))
    }

    @Test
    fun longestStreak_findsTheRecord() {
        val h = habit("2026-10-10", "2026-10-11", "2026-10-12", "2026-10-13", "2026-11-05", "2026-11-06")
        assertEquals(4, h.longestStreak(today))
    }

    @Test
    fun period_followsReminderTime() {
        assertEquals(DayPeriod.MORNING, habit().copy(reminderTime = LocalTime.of(7, 0)).period)
        assertEquals(DayPeriod.NIGHT, habit().copy(reminderTime = LocalTime.of(21, 0)).period)
    }

    @Test
    fun summary_combinesGoalAndTime() {
        assertEquals("2L • 08:00", habit().copy(goal = "2L").summary)
        assertEquals("08:00", habit().summary)
    }

    @Test
    fun stats_weekStartsOnMondayAndRatesOnlyScheduledDays() {
        val week = HabitStats.weekOf(today)
        assertEquals(LocalDate.of(2026, 11, 2), week.first())
        assertEquals(LocalDate.of(2026, 11, 8), week.last())

        val habits = listOf(habit("2026-11-02", "2026-11-03"), habit("2026-11-02"))
        val monTue = week.take(2)
        assertEquals(0.75f, HabitStats.completionRate(habits, monTue)!!, 0.001f)
        assertEquals(3, HabitStats.checkIns(habits, week))
        assertNull(HabitStats.completionRate(emptyList(), week))
    }
}
