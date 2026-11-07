package com.example.habits.ui.habits.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habits.AppContainer
import com.example.habits.data.model.DayPeriod
import com.example.habits.data.model.HabitCategory
import com.example.habits.data.repository.HabitRepository
import com.example.habits.ui.components.HabitCardUi
import com.example.habits.ui.home.toCardUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitSection(val period: DayPeriod, val habits: List<HabitCardUi>)

data class HabitListUiState(
    val activeCount: Int = 0,
    val doneTodayCount: Int = 0,
    /** null = "Todos". */
    val selectedCategory: HabitCategory? = null,
    val sections: List<HabitSection> = emptyList(),
    val isLoading: Boolean = true,
)

/** ViewModel de "Meus hábitos": todos os hábitos, filtro por categoria e agrupamento por período. */
class HabitListViewModel(
    private val habitRepository: HabitRepository,
    private val userId: String,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<HabitCategory?>(null)

    val uiState: StateFlow<HabitListUiState> =
        combine(habitRepository.observeHabits(userId), selectedCategory) { habits, category ->
            val today = LocalDate.now()
            val filtered = habits.filter { category == null || it.category == category }
            HabitListUiState(
                activeCount = habits.size,
                doneTodayCount = habits.count { it.isDoneOn(today) },
                selectedCategory = category,
                sections = filtered
                    .groupBy { it.period }
                    .toSortedMap()
                    .map { (period, list) -> HabitSection(period, list.map { it.toCardUi(today) }) },
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListUiState())

    fun selectCategory(category: HabitCategory?) {
        selectedCategory.value = category
    }

    fun toggleToday(habitId: String) {
        viewModelScope.launch { habitRepository.toggleCompletion(userId, habitId, LocalDate.now()) }
    }

    companion object {
        fun factory(container: AppContainer, userId: String) = viewModelFactory {
            initializer { HabitListViewModel(container.habitRepository, userId) }
        }
    }
}
