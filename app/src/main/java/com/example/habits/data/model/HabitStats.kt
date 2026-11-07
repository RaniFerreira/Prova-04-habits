package com.example.habits.data.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

/** Cálculos de progresso e estatísticas (funções puras, testáveis sem Android). */
object HabitStats {

    data class DayProgress(val date: LocalDate, val scheduled: Int, val done: Int) {
        val rate: Float? get() = if (scheduled == 0) null else done.toFloat() / scheduled
    }

    /** Segunda a domingo da semana que contém [date]. */
    fun weekOf(date: LocalDate): List<LocalDate> {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0L..6L).map { monday.plusDays(it) }
    }

    fun dayProgress(habits: List<Habit>, date: LocalDate): DayProgress {
        val scheduled = habits.filter { it.isScheduledOn(date) }
        return DayProgress(date, scheduled.size, scheduled.count { it.isDoneOn(date) })
    }

    /** Taxa de conclusão (0..1) nos dias informados, ou null se nada estava programado. */
    fun completionRate(habits: List<Habit>, days: List<LocalDate>): Float? {
        var scheduled = 0
        var done = 0
        days.forEach { day ->
            val progress = dayProgress(habits, day)
            scheduled += progress.scheduled
            done += progress.done
        }
        return if (scheduled == 0) null else done.toFloat() / scheduled
    }

    /** Taxa geral desde a criação de cada hábito até hoje. */
    fun overallRate(habits: List<Habit>, today: LocalDate): Float? {
        val start = habits.minOfOrNull { it.createdDate } ?: return null
        val days = generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(today) }.toList()
        return completionRate(habits, days)
    }

    fun checkIns(habits: List<Habit>, days: List<LocalDate>): Int =
        days.sumOf { day -> habits.count { it.isDoneOn(day) } }

    fun percent(rate: Float?): Int = ((rate ?: 0f) * 100).roundToInt()
}
