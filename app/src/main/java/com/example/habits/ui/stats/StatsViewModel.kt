package com.example.habits.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habits.AppContainer
import com.example.habits.data.model.Habit
import com.example.habits.data.model.HabitCategory
import com.example.habits.data.model.HabitStats
import com.example.habits.data.repository.HabitRepository
import com.example.habits.ui.components.rangeLabel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import kotlin.math.roundToInt

data class BarDay(val date: LocalDate, val rate: Float?, val isToday: Boolean, val isFuture: Boolean)

enum class AchievementIcon { SPARKLE, FLAME, LEAF, CALENDAR, TROPHY }

data class Achievement(val title: String, val description: String, val icon: AchievementIcon, val unlocked: Boolean)

data class StatsUiState(
    val completionPercent: Int = 0,
    val bestStreak: Int = 0,
    val weekCheckIns: Int = 0,
    val rangeLabel: String = "",
    val days: List<BarDay> = emptyList(),
    /** Variação em pontos percentuais em relação à semana anterior (null se não houver base). */
    val deltaVsLastWeek: Int? = null,
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true,
) {
    /** As três conquistas exibidas na tela (as demais aparecem em "Ver todas"). */
    val featuredAchievements: List<Achievement> get() = achievements.take(3)
}

/** ViewModel de Estatísticas: taxa de conclusão, sequência, check-ins e gráfico da semana. */
class StatsViewModel(habitRepository: HabitRepository, userId: String) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = habitRepository.observeHabits(userId)
        .map { habits -> buildState(habits, LocalDate.now()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    companion object {
        fun buildState(habits: List<Habit>, today: LocalDate): StatsUiState {
            val week = HabitStats.weekOf(today)
            val elapsed = week.filter { !it.isAfter(today) }
            val lastWeek = HabitStats.weekOf(today.minusWeeks(1))
            val rate = HabitStats.completionRate(habits, elapsed)
            val lastRate = HabitStats.completionRate(habits, lastWeek)
            val totalCheckIns = habits.sumOf { it.totalCompletions }
            val longest = habits.maxOfOrNull { it.longestStreak(today) } ?: 0
            val mindCheckIns = habits.filter { it.category == HabitCategory.MIND }.sumOf { it.totalCompletions }

            return StatsUiState(
                completionPercent = HabitStats.percent(rate),
                bestStreak = habits.maxOfOrNull { it.currentStreak(today) } ?: 0,
                weekCheckIns = HabitStats.checkIns(habits, week),
                rangeLabel = rangeLabel(week.first(), week.last()),
                days = week.map { date ->
                    BarDay(
                        date = date,
                        rate = if (date.isAfter(today)) null else HabitStats.dayProgress(habits, date).rate,
                        isToday = date == today,
                        isFuture = date.isAfter(today),
                    )
                },
                deltaVsLastWeek = if (rate != null && lastRate != null) ((rate - lastRate) * 100).roundToInt() else null,
                achievements = listOf(
                    Achievement("Primeiro passo", "Conclua um hábito pela primeira vez.", AchievementIcon.SPARKLE, totalCheckIns >= 1),
                    Achievement("7 dias seguidos", "Mantenha uma sequência de 7 dias.", AchievementIcon.FLAME, longest >= 7),
                    Achievement("Mestre zen", "Conclua 10 hábitos da categoria Mente.", AchievementIcon.LEAF, mindCheckIns >= 10),
                    Achievement("Mês consistente", "Mantenha uma sequência de 30 dias.", AchievementIcon.CALENDAR, longest >= 30),
                    Achievement("Centenário", "Alcance 100 check-ins no total.", AchievementIcon.TROPHY, totalCheckIns >= 100),
                ),
                isLoading = false,
            )
        }

        fun factory(container: AppContainer, userId: String) = viewModelFactory {
            initializer { StatsViewModel(container.habitRepository, userId) }
        }
    }
}
