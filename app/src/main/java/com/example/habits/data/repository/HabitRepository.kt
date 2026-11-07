package com.example.habits.data.repository

import com.example.habits.data.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Contrato do armazenamento de hábitos. Os dados são separados por usuário (uid). */
interface HabitRepository {
    fun observeHabits(userId: String): Flow<List<Habit>>
    fun observeHabit(userId: String, habitId: String): Flow<Habit?>
    suspend fun upsert(userId: String, habit: Habit)
    suspend fun delete(userId: String, habitId: String)
    suspend fun toggleCompletion(userId: String, habitId: String, date: LocalDate)
}
