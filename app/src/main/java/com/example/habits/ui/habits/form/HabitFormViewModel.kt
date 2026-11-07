package com.example.habits.ui.habits.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habits.AppContainer
import com.example.habits.data.model.Habit
import com.example.habits.data.model.HabitCategory
import com.example.habits.data.model.HabitFrequency
import com.example.habits.data.model.HabitIcon
import com.example.habits.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class HabitFormUiState(
    val habitId: String? = null,
    val isLoading: Boolean = false,
    val name: String = "",
    val description: String = "",
    val goal: String = "",
    val category: HabitCategory = HabitCategory.HEALTH,
    val icon: HabitIcon = HabitIcon.WATER,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val reminderTime: LocalTime = LocalTime.of(8, 0),
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
) {
    val isNew: Boolean get() = habitId == null
}

/** ViewModel do formulário de cadastro/edição de hábito. */
class HabitFormViewModel(
    private val habitRepository: HabitRepository,
    private val userId: String,
    private val habitId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitFormUiState(habitId = habitId, isLoading = habitId != null))
    val uiState: StateFlow<HabitFormUiState> = _uiState.asStateFlow()

    private var original: Habit? = null

    init {
        if (habitId != null) {
            viewModelScope.launch {
                val habit = habitRepository.observeHabit(userId, habitId).first()
                original = habit
                _uiState.update { state ->
                    if (habit == null) state.copy(isLoading = false, isSaved = true)
                    else state.copy(
                        isLoading = false,
                        name = habit.name,
                        description = habit.description,
                        goal = habit.goal,
                        category = habit.category,
                        icon = habit.icon,
                        frequency = habit.frequency,
                        days = habit.activeDays,
                        reminderTime = habit.reminderTime,
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onGoalChange(value: String) = _uiState.update { it.copy(goal = value) }
    fun onCategoryChange(value: HabitCategory) = _uiState.update { it.copy(category = value) }
    fun onIconChange(value: HabitIcon) = _uiState.update { it.copy(icon = value) }
    fun onTimeChange(hour: Int, minute: Int) = _uiState.update { it.copy(reminderTime = LocalTime.of(hour, minute)) }

    fun onFrequencyChange(value: HabitFrequency) = _uiState.update { state ->
        val days = when (value) {
            HabitFrequency.DAILY -> DayOfWeek.entries.toSet()
            HabitFrequency.WEEKLY -> setOf(state.days.firstOrNull { state.days.size < 7 } ?: LocalDate.now().dayOfWeek)
            HabitFrequency.CUSTOM -> if (state.days.size == 7) DayOfWeek.entries.toSet() - DayOfWeek.SUNDAY - DayOfWeek.SATURDAY else state.days
        }
        state.copy(frequency = value, days = days, errorMessage = null)
    }

    fun onDayToggle(day: DayOfWeek) = _uiState.update { state ->
        val days = if (day in state.days) state.days - day else state.days + day
        val frequency = when {
            days.size == 7 -> HabitFrequency.DAILY
            state.frequency == HabitFrequency.DAILY -> HabitFrequency.CUSTOM
            state.frequency == HabitFrequency.WEEKLY && days.size > 1 -> HabitFrequency.CUSTOM
            else -> state.frequency
        }
        state.copy(days = days, frequency = frequency, errorMessage = null)
    }

    fun save() {
        val state = _uiState.value
        val name = state.name.trim()
        when {
            name.isEmpty() -> return _uiState.update { it.copy(errorMessage = "Dê um nome ao hábito.") }
            state.days.isEmpty() -> return _uiState.update { it.copy(errorMessage = "Escolha pelo menos um dia da semana.") }
        }
        val base = original ?: Habit(name = name)
        val habit = base.copy(
            name = name,
            description = state.description.trim(),
            goal = state.goal.trim(),
            category = state.category,
            icon = state.icon,
            frequency = state.frequency,
            days = state.days,
            reminderTime = state.reminderTime,
        )
        viewModelScope.launch {
            habitRepository.upsert(userId, habit)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    companion object {
        fun factory(container: AppContainer, userId: String, habitId: String?) = viewModelFactory {
            initializer { HabitFormViewModel(container.habitRepository, userId, habitId) }
        }
    }
}
