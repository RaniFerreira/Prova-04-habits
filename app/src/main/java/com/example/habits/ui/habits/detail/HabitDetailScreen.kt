package com.example.habits.ui.habits.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.data.model.Habit
import com.example.habits.data.model.HabitStats
import com.example.habits.ui.components.GradientButton
import com.example.habits.ui.components.GradientHeader
import com.example.habits.ui.components.OutlineButton
import com.example.habits.ui.components.SoftButton
import com.example.habits.ui.components.SquareIconButton
import com.example.habits.ui.components.StatusBarIcons
import com.example.habits.ui.components.Tag
import com.example.habits.ui.components.habitIconRotation
import com.example.habits.ui.components.shortLabel
import com.example.habits.ui.components.vector
import com.example.habits.ui.theme.Amber
import com.example.habits.ui.theme.AmberSoft
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.BorderColor
import com.example.habits.ui.theme.CardWhite
import com.example.habits.ui.theme.Green
import com.example.habits.ui.theme.GreenSoft
import com.example.habits.ui.theme.GreenText
import com.example.habits.ui.theme.HabitsTheme
import com.example.habits.ui.theme.Purple100
import com.example.habits.ui.theme.Purple50
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.Red
import com.example.habits.ui.theme.RedSoft
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary
import java.time.LocalDate

@Composable
fun HabitDetailRoute(viewModel: HabitDetailViewModel, onBack: () -> Unit, onEdit: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) { if (state.isFinished) onBack() }
    HabitDetailScreen(
        state = state,
        onBack = onBack,
        onEdit = { state.habit?.let { onEdit(it.id) } },
        onToggleToday = viewModel::toggleToday,
        onRequestDelete = viewModel::requestDelete,
        onDismissDelete = viewModel::dismissDelete,
        onConfirmDelete = viewModel::confirmDelete,
    )
}

@Composable
fun HabitDetailScreen(
    state: HabitDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleToday: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    StatusBarIcons(dark = false)
    val habit = state.habit
    if (habit == null) {
        Box(Modifier.fillMaxSize().background(AppBackground), Alignment.Center) { CircularProgressIndicator(color = Purple600) }
        return
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        Header(habit, onBack, onEdit, onRequestDelete)
        Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TodayStatusCard(habit, state.isDoneToday)
            WeeklyHistoryCard(state)
            if (state.isDoneToday) {
                OutlineButton("Desfazer conclusão", onToggleToday, icon = Icons.Rounded.Close)
            } else {
                GradientButton("Marcar como concluído", onToggleToday, icon = Icons.Rounded.Check)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SoftButton("Editar", Icons.Outlined.Edit, Purple100, Purple600, onEdit, Modifier.weight(1f))
                SoftButton("Excluir", Icons.Outlined.Delete, RedSoft, Red, onRequestDelete, Modifier.weight(1f))
            }
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            containerColor = CardWhite,
            title = { Text("Excluir hábito?") },
            text = { Text("\"${habit.name}\" e todo o histórico serão removidos.") },
            confirmButton = { TextButton(onClick = onConfirmDelete) { Text("Excluir", color = Red) } },
            dismissButton = { TextButton(onClick = onDismissDelete) { Text("Cancelar", color = TextSecondary) } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(habit: Habit, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    GradientHeader {
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar", onBack, Modifier.align(Alignment.CenterStart), tint = Purple600)
                Text("Detalhes", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Box(Modifier.align(Alignment.CenterEnd)) {
                    SquareIconButton(Icons.Rounded.MoreVert, "Mais opções", { menuOpen = true }, tint = Purple600)
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = CardWhite) {
                        DropdownMenuItem(text = { Text("Editar") }, onClick = { menuOpen = false; onEdit() })
                        DropdownMenuItem(text = { Text("Excluir", color = Red) }, onClick = { menuOpen = false; onDelete() })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .size(68.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(habit.icon.vector, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp).habitIconRotation(habit.icon))
            }
            Spacer(Modifier.height(14.dp))
            Text(habit.name, style = MaterialTheme.typography.headlineLarge, color = Color.White, textAlign = TextAlign.Center)
            if (habit.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    habit.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(14.dp))
            val chips = listOfNotNull(
                habit.category.label,
                habit.frequency.label,
                habit.reminderLabel,
                habit.goal.takeIf { it.isNotBlank() }?.let { "Meta: $it" },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chips.forEach { Tag(it, Color.White.copy(alpha = 0.18f), Color.White, Modifier.padding(vertical = 2.dp)) }
            }
        }
    }
}

@Composable
private fun WhiteCard(content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        content()
    }
}

@Composable
private fun TodayStatusCard(habit: Habit, done: Boolean) {
    WhiteCard {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(if (done) GreenSoft else AmberSoft, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (done) Icons.Rounded.Check else Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = if (done) Green else Amber,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Status de hoje", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(if (done) "Concluído" else "Pendente", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }
            if (done) Tag("Feito hoje", GreenSoft, GreenText)
            else Tag(if (habit.goal.isBlank()) "Às ${habit.reminderLabel}" else "Meta: ${habit.goal}", AmberSoft, Amber)
        }
    }
}

@Composable
private fun WeeklyHistoryCard(state: HabitDetailUiState) {
    WhiteCard {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("Histórico semanal", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text(
                        "${state.weekDone} de ${state.weekScheduled} dias concluídos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                Tag(
                    "${state.streak} ${if (state.streak == 1) "dia" else "dias"}",
                    AmberSoft,
                    Amber,
                    icon = Icons.Outlined.LocalFireDepartment,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                state.week.forEach { day -> DayCircle(day) }
            }
        }
    }
}

@Composable
private fun DayCircle(day: WeekDayUi) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            when (day.state) {
                DayState.DONE -> Box(Modifier.size(36.dp).background(Green, CircleShape), Alignment.Center) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                DayState.MISSED -> Box(Modifier.size(36.dp).background(RedSoft, CircleShape), Alignment.Center) {
                    Icon(Icons.Rounded.Close, null, tint = Red, modifier = Modifier.size(18.dp))
                }
                DayState.TODAY -> Box(Modifier.size(36.dp).background(Purple50, CircleShape).border(2.dp, Purple600, CircleShape))
                DayState.FUTURE, DayState.OFF -> Canvas(Modifier.size(36.dp)) {
                    drawCircle(
                        BorderColor,
                        radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            day.date.dayOfWeek.shortLabel,
            style = MaterialTheme.typography.bodySmall,
            color = if (day.state == DayState.TODAY) Purple600 else TextSecondary,
        )
    }
}

@Preview(heightDp = 900)
@Composable
private fun HabitDetailPreview() {
    val today = LocalDate.of(2026, 11, 6)
    val habit = Habit(
        name = "Beber água", description = "Manter o corpo hidratado ao longo do dia.", goal = "2L",
        createdAt = 0, completedDates = setOf("2026-11-02", "2026-11-03", "2026-11-05"),
    )
    val week = HabitStats.weekOf(today).map {
        WeekDayUi(
            it,
            when {
                habit.isDoneOn(it) -> DayState.DONE
                it.isAfter(today) -> DayState.FUTURE
                it == today -> DayState.TODAY
                else -> DayState.MISSED
            },
        )
    }
    HabitsTheme {
        HabitDetailScreen(
            HabitDetailUiState(habit = habit, isLoading = false, streak = 12, week = week, weekDone = 4, weekScheduled = 5),
            {}, {}, {}, {}, {}, {},
        )
    }
}
