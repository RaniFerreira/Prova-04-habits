package com.example.habits.ui.habits.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.data.model.DayPeriod
import com.example.habits.data.model.HabitCategory
import com.example.habits.data.model.HabitIcon
import com.example.habits.ui.components.GradientFab
import com.example.habits.ui.components.HabitCard
import com.example.habits.ui.components.HabitCardUi
import com.example.habits.ui.components.SelectChip
import com.example.habits.ui.components.SmallCaps
import com.example.habits.ui.components.SquareIconButton
import com.example.habits.ui.components.StatusBarIcons
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.HabitsTheme
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary

@Composable
fun HabitListRoute(
    viewModel: HabitListViewModel,
    onOpenHabit: (String) -> Unit,
    onAddHabit: () -> Unit,
    onOpenFilters: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HabitListScreen(state, viewModel::selectCategory, viewModel::toggleToday, onOpenHabit, onAddHabit, onOpenFilters)
}

@Composable
fun HabitListScreen(
    state: HabitListUiState,
    onSelectCategory: (HabitCategory?) -> Unit,
    onToggle: (String) -> Unit,
    onOpenHabit: (String) -> Unit,
    onAddHabit: () -> Unit,
    onOpenFilters: () -> Unit,
) {
    StatusBarIcons(dark = true)
    Box(Modifier.fillMaxSize().background(AppBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Meus hábitos", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                        Text(
                            "${state.activeCount} hábitos ativos • ${state.doneTodayCount} concluídos hoje",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                    SquareIconButton(Icons.Outlined.Tune, "Filtros", onOpenFilters)
                }
            }
            item {
                val categories = listOf<HabitCategory?>(null) + HabitCategory.entries
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    items(categories) { category ->
                        SelectChip(
                            text = category?.label ?: "Todos",
                            selected = state.selectedCategory == category,
                            onClick = { onSelectCategory(category) },
                        )
                    }
                }
            }
            when {
                state.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) { CircularProgressIndicator(color = Purple600) }
                }
                state.sections.isEmpty() -> item {
                    Text(
                        if (state.activeCount == 0) "Você ainda não tem hábitos. Toque em + para criar o primeiro."
                        else "Nenhum hábito nesta categoria.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                    )
                }
                else -> state.sections.forEach { section ->
                    item(key = section.period.name) {
                        SmallCaps(section.period.label, Modifier.padding(start = 24.dp, top = 6.dp))
                    }
                    items(section.habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit,
                            onToggle = { onToggle(habit.id) },
                            onClick = { onOpenHabit(habit.id) },
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                }
            }
        }
        GradientFab(onAddHabit, Modifier.align(Alignment.BottomEnd).padding(24.dp))
    }
}

@Preview(heightDp = 800)
@Composable
private fun HabitListPreview() {
    HabitsTheme {
        HabitListScreen(
            HabitListUiState(
                activeCount = 5, doneTodayCount = 3, isLoading = false,
                sections = listOf(
                    HabitSection(
                        DayPeriod.MORNING,
                        listOf(
                            HabitCardUi("1", "Beber água", "2L • 08:00", HabitIcon.WATER, true),
                            HabitCardUi("2", "Exercícios", "30 min • 07:00", HabitIcon.DUMBBELL, true),
                        ),
                    ),
                    HabitSection(
                        DayPeriod.NIGHT,
                        listOf(
                            HabitCardUi("3", "Ler", "20 páginas • 21:00", HabitIcon.BOOK, true),
                            HabitCardUi("4", "Meditar", "10 min • 22:00", HabitIcon.LEAF, false),
                            HabitCardUi("5", "Dormir cedo", "8h de sono • 23:00", HabitIcon.MOON, false),
                        ),
                    ),
                ),
            ),
            {}, {}, {}, {}, {},
        )
    }
}
