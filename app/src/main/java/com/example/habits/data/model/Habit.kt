package com.example.habits.data.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class HabitCategory(val label: String) {
    HEALTH("Saúde"), MIND("Mente"), PRODUCTIVITY("Produtividade")
}

enum class HabitIcon { WATER, BOOK, DUMBBELL, LEAF, MOON, FLAME }

enum class HabitFrequency(val label: String) {
    DAILY("Diário"), WEEKLY("Semanal"), CUSTOM("Personalizado")
}

enum class DayPeriod(val label: String) {
    MORNING("MANHÃ"), AFTERNOON("TARDE"), NIGHT("NOITE")
}

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Model: hábito do usuário.
 *
 * O status diário (feito / não feito) é guardado como conjunto de datas ISO-8601
 * ("2026-11-07") em que o hábito foi concluído. A partir disso são calculados o
 * status de hoje, a sequência (streak), o histórico semanal e as estatísticas.
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val goal: String = "",
    val category: HabitCategory = HabitCategory.HEALTH,
    val icon: HabitIcon = HabitIcon.WATER,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val reminderTime: LocalTime = LocalTime.of(8, 0),
    val createdAt: Long = System.currentTimeMillis(),
    val completedDates: Set<String> = emptySet(),
) {
    val createdDate: LocalDate
        get() = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()

    /** Dias da semana em que o hábito deve ser feito. */
    val activeDays: Set<DayOfWeek>
        get() = if (frequency == HabitFrequency.DAILY) DayOfWeek.entries.toSet() else days

    val reminderLabel: String get() = reminderTime.format(TimeFormatter)

    /** Ex.: "2L • 08:00" (meta + horário), como nos cards do protótipo. */
    val summary: String get() = listOf(goal.trim(), reminderLabel).filter { it.isNotEmpty() }.joinToString(" • ")

    val period: DayPeriod
        get() = when (reminderTime.hour) {
            in 5..11 -> DayPeriod.MORNING
            in 12..17 -> DayPeriod.AFTERNOON
            else -> DayPeriod.NIGHT
        }

    fun isScheduledOn(date: LocalDate): Boolean =
        !date.isBefore(createdDate) && date.dayOfWeek in activeDays

    fun isDoneOn(date: LocalDate): Boolean = date.toString() in completedDates

    /** Alterna o status do dia informado entre feito e não feito. */
    fun toggle(date: LocalDate): Habit {
        val key = date.toString()
        val updated = if (key in completedDates) completedDates - key else completedDates + key
        return copy(completedDates = updated)
    }

    /**
     * Sequência atual: dias programados concluídos em sequência até hoje.
     * Dias fora da programação não quebram a sequência, e hoje ainda pendente também não.
     */
    fun currentStreak(today: LocalDate): Int {
        var day = if (isDoneOn(today)) today else today.minusDays(1)
        var streak = 0
        while (true) {
            when {
                isDoneOn(day) -> streak++
                day.isBefore(createdDate) -> break
                isScheduledOn(day) -> break
            }
            day = day.minusDays(1)
        }
        return streak
    }

    /** Maior sequência já alcançada (recorde). */
    fun longestStreak(today: LocalDate): Int {
        val firstCompletion = completedDates.minOrNull()?.let(LocalDate::parse) ?: return 0
        var day = minOf(firstCompletion, createdDate)
        var best = 0
        var current = 0
        while (!day.isAfter(today)) {
            if (isDoneOn(day)) {
                current++
                best = maxOf(best, current)
            } else if (isScheduledOn(day) && day != today) {
                current = 0
            }
            day = day.plusDays(1)
        }
        return best
    }

    val totalCompletions: Int get() = completedDates.size
}
