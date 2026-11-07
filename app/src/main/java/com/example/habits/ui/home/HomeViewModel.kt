package com.example.habits.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habits.AppContainer
import com.example.habits.data.model.Habit
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.HabitRepository
import com.example.habits.ui.components.HabitCardUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val firstName: String = "",
    val initials: String = "",
    val today: LocalDate = LocalDate.now(),
    val todayHabits: List<HabitCardUi> = emptyList(),
    val isLoading: Boolean = true,
) {
    val doneCount: Int get() = todayHabits.count { it.isDone }
    val total: Int get() = todayHabits.size
    val progress: Float get() = if (total == 0) 0f else doneCount.toFloat() / total

    /** Frase do card de progresso ("Você está indo muito bem!"). */
    val headline: String
        get() = when {
            total == 0 -> "Nenhum hábito para hoje"
            progress >= 1f -> "Parabéns, tudo concluído!"
            progress >= 0.5f -> "Você está indo muito bem!"
            progress > 0f -> "Bom começo, continue!"
            else -> "Vamos começar o dia?"
        }
}

/** ViewModel da tela Início: hábitos programados para hoje e progresso do dia. */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
    private val userId: String,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(authRepository.currentUser, habitRepository.observeHabits(userId)) { user, habits ->
            val today = LocalDate.now()
            HomeUiState(
                firstName = user?.firstName.orEmpty(),
                initials = user?.initials.orEmpty(),
                today = today,
                todayHabits = habits
                    .filter { it.isScheduledOn(today) || it.isDoneOn(today) }
                    .map { it.toCardUi(today) },
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleToday(habitId: String) {
        viewModelScope.launch { habitRepository.toggleCompletion(userId, habitId, LocalDate.now()) }
    }

    companion object {
        fun factory(container: AppContainer, userId: String) = viewModelFactory {
            initializer { HomeViewModel(container.authRepository, container.habitRepository, userId) }
        }
    }
}

fun Habit.toCardUi(today: LocalDate) = HabitCardUi(
    id = id,
    name = name,
    summary = summary,
    icon = icon,
    isDone = isDoneOn(today),
)
