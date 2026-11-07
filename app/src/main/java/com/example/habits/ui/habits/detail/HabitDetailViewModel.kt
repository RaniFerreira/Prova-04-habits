package com.example.habits.ui.habits.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habits.AppContainer
import com.example.habits.data.model.Habit
import com.example.habits.data.model.HabitStats
import com.example.habits.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class DayState { DONE, MISSED, TODAY, FUTURE, OFF }

data class WeekDayUi(val date: LocalDate, val state: DayState)

data class HabitDetailUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true,
    val isDoneToday: Boolean = false,
    val streak: Int = 0,
    val week: List<WeekDayUi> = emptyList(),
    val weekDone: Int = 0,
    val weekScheduled: Int = 0,
    val showDeleteDialog: Boolean = false,
    val isFinished: Boolean = false,
)

/** ViewModel de Detalhes: status de hoje, histórico semanal, streak, marcar/desmarcar e excluir. */
class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val userId: String,
    private val habitId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            habitRepository.observeHabit(userId, habitId).collect { habit ->
                _uiState.update { state ->
                    if (habit == null) state.copy(isLoading = false, isFinished = true)
                    else buildState(habit).copy(showDeleteDialog = state.showDeleteDialog)
                }
            }
        }
    }

    private fun buildState(habit: Habit): HabitDetailUiState {
        val today = LocalDate.now()
        val week = HabitStats.weekOf(today).map { date ->
            val state = when {
                habit.isDoneOn(date) -> DayState.DONE
                date.isAfter(today) -> DayState.FUTURE
                date == today -> DayState.TODAY
                habit.isScheduledOn(date) -> DayState.MISSED
                else -> DayState.OFF
            }
            WeekDayUi(date, state)
        }
        val elapsed = week.filter { !it.date.isAfter(today) }
        return HabitDetailUiState(
            habit = habit,
            isLoading = false,
            isDoneToday = habit.isDoneOn(today),
            streak = habit.currentStreak(today),
            week = week,
            weekDone = elapsed.count { it.state == DayState.DONE },
            weekScheduled = elapsed.count { habit.isScheduledOn(it.date) || it.state == DayState.DONE },
        )
    }

    fun toggleToday() {
        viewModelScope.launch { habitRepository.toggleCompletion(userId, habitId, LocalDate.now()) }
    }

    fun requestDelete() = _uiState.update { it.copy(showDeleteDialog = true) }
    fun dismissDelete() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun confirmDelete() {
        _uiState.update { it.copy(showDeleteDialog = false) }
        viewModelScope.launch { habitRepository.delete(userId, habitId) }
    }

    companion object {
        fun factory(container: AppContainer, userId: String, habitId: String) = viewModelFactory {
            initializer { HabitDetailViewModel(container.habitRepository, userId, habitId) }
        }
    }
}
