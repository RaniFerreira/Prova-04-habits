package com.example.habits.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.data.model.HabitIcon
import com.example.habits.ui.components.GradientFab
import com.example.habits.ui.components.HabitCard
import com.example.habits.ui.components.HabitCardUi
import com.example.habits.ui.components.SectionHeader
import com.example.habits.ui.components.SmallCaps
import com.example.habits.ui.components.SquareIconButton
import com.example.habits.ui.components.StatusBarIcons
import com.example.habits.ui.components.headerLabel
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.BrandGradient
import com.example.habits.ui.theme.HabitsTheme
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary
import java.time.LocalDate

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onOpenHabit: (String) -> Unit,
    onAddHabit: () -> Unit,
    onSeeAll: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state, viewModel::toggleToday, onOpenHabit, onAddHabit, onSeeAll, onOpenProfile, onOpenNotifications)
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onToggle: (String) -> Unit,
    onOpenHabit: (String) -> Unit,
    onAddHabit: () -> Unit,
    onSeeAll: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    StatusBarIcons(dark = true)
    Box(Modifier.fillMaxSize().background(AppBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(state.today.headerLabel(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Text(
                            if (state.firstName.isBlank()) "Olá!" else "Olá, ${state.firstName}!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                        )
                    }
                    SquareIconButton(Icons.Outlined.Notifications, "Notificações", onOpenNotifications)
                    Spacer(Modifier.width(12.dp))
                    Avatar(state.initials, onOpenProfile)
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                ProgressCard(state)
                Spacer(Modifier.height(12.dp))
            }
            item { SectionHeader("Hábitos de hoje", action = "Ver todos", onAction = onSeeAll) }
            when {
                state.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) { CircularProgressIndicator(color = Purple600) }
                }
                state.todayHabits.isEmpty() -> item {
                    Text(
                        "Nenhum hábito programado para hoje. Toque em + para criar um.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    )
                }
                else -> items(state.todayHabits, key = { it.id }) { habit ->
                    HabitCard(habit, onToggle = { onToggle(habit.id) }, onClick = { onOpenHabit(habit.id) })
                }
            }
        }
        GradientFab(onAddHabit, Modifier.align(Alignment.BottomEnd).padding(24.dp))
    }
}

@Composable
private fun Avatar(initials: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(8.dp, CircleShape, ambientColor = Purple600, spotColor = Purple600)
            .clip(CircleShape)
            .background(BrandGradient)
            .clickable(onClickLabel = "Abrir perfil", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials.take(1), style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
private fun ProgressCard(state: HomeUiState) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, ambientColor = Purple600, spotColor = Purple600)
            .clip(shape)
            .background(BrandGradient),
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = 0.07f), radius = size.height * 0.75f, center = Offset(size.width * 0.92f, 0f))
            drawCircle(Color.White.copy(alpha = 0.06f), radius = size.height * 0.35f, center = Offset(size.width * 0.12f, size.height))
        }
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SmallCaps("Progresso de hoje", color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(6.dp))
                Text(state.headline, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${state.doneCount} de ${state.total} hábitos concluídos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f))) {
                    Box(Modifier.fillMaxWidth(state.progress).height(8.dp).clip(CircleShape).background(Color.White))
                }
            }
            Spacer(Modifier.width(18.dp))
            ProgressRing(state.progress)
        }
    }
}

@Composable
private fun ProgressRing(progress: Float) {
    Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = 10.dp.toPx()
            drawArc(Color.White.copy(alpha = 0.25f), 0f, 360f, false, style = Stroke(stroke))
            drawArc(Color.White, -90f, 360f * progress, false, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("hoje", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Preview(heightDp = 800)
@Composable
private fun HomePreview() {
    HabitsTheme {
        HomeScreen(
            HomeUiState(
                firstName = "Rani", initials = "RF", today = LocalDate.of(2026, 11, 6), isLoading = false,
                todayHabits = listOf(
                    HabitCardUi("1", "Beber água", "2L • 08:00", HabitIcon.WATER, true),
                    HabitCardUi("2", "Exercícios", "30 min • 07:00", HabitIcon.DUMBBELL, true),
                    HabitCardUi("3", "Ler", "20 páginas • 21:00", HabitIcon.BOOK, true),
                    HabitCardUi("4", "Meditar", "10 min • 22:00", HabitIcon.LEAF, false),
                ),
            ),
            {}, {}, {}, {}, {}, {},
        )
    }
}
