package com.example.habits.ui.habits.form

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.data.model.HabitCategory
import com.example.habits.data.model.HabitFrequency
import com.example.habits.data.model.HabitIcon
import com.example.habits.ui.components.BackTitleBar
import com.example.habits.ui.components.ErrorText
import com.example.habits.ui.components.FieldLabel
import com.example.habits.ui.components.GradientButton
import com.example.habits.ui.components.HfPickerField
import com.example.habits.ui.components.HfTextField
import com.example.habits.ui.components.SelectChip
import com.example.habits.ui.components.StatusBarIcons
import com.example.habits.ui.components.habitIconRotation
import com.example.habits.ui.components.initial
import com.example.habits.ui.components.vector
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.BorderColor
import com.example.habits.ui.theme.CardWhite
import com.example.habits.ui.theme.HabitsTheme
import com.example.habits.ui.theme.Purple50
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

/** Ordem do protótipo: D S T Q Q S S. */
private val WeekOrder = listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }

@Composable
fun HabitFormRoute(viewModel: HabitFormViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }
    HabitFormScreen(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onGoalChange = viewModel::onGoalChange,
        onCategoryChange = viewModel::onCategoryChange,
        onIconChange = viewModel::onIconChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onDayToggle = viewModel::onDayToggle,
        onTimeChange = viewModel::onTimeChange,
        onSave = viewModel::save,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitFormScreen(
    state: HabitFormUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onCategoryChange: (HabitCategory) -> Unit,
    onIconChange: (HabitIcon) -> Unit,
    onFrequencyChange: (HabitFrequency) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onSave: () -> Unit,
) {
    StatusBarIcons(dark = true)
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        BackTitleBar(
            if (state.isNew) "Novo hábito" else "Editar hábito",
            onBack,
            Icons.AutoMirrored.Rounded.ArrowBack,
            Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Purple600) }
            return@Column
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            FieldLabel("Nome do hábito")
            HfTextField(state.name, onNameChange, "Ex.: Beber água", Icons.Outlined.TrackChanges)
            Spacer(Modifier.height(16.dp))
            FieldLabel("Descrição")
            HfTextField(state.description, onDescriptionChange, "Por que esse hábito é importante?", Icons.Outlined.Edit, singleLine = false)
            Spacer(Modifier.height(16.dp))
            FieldLabel("Meta (opcional)")
            HfTextField(state.goal, onGoalChange, "Ex.: 2L, 30 min, 20 páginas", Icons.Outlined.Flag)
            Spacer(Modifier.height(16.dp))

            FieldLabel("Categoria")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitCategory.entries.forEach { category ->
                    SelectChip(category.label, state.category == category, { onCategoryChange(category) })
                }
            }
            Spacer(Modifier.height(16.dp))

            FieldLabel("Ícone")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitIcon.entries.forEach { icon ->
                    IconOption(icon, selected = state.icon == icon, onClick = { onIconChange(icon) }, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))

            FieldLabel("Frequência")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitFrequency.entries.forEach { frequency ->
                    SelectChip(
                        frequency.label,
                        state.frequency == frequency,
                        { onFrequencyChange(frequency) },
                        icon = Icons.Outlined.Repeat,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            FieldLabel("Dias da semana")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeekOrder.forEach { day ->
                    DayOption(day.initial, selected = day in state.days, onClick = { onDayToggle(day) }, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))

            FieldLabel("Horário do lembrete")
            HfPickerField(
                text = state.reminderTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                leadingIcon = Icons.Outlined.Schedule,
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onTimeChange(hour, minute) },
                        state.reminderTime.hour,
                        state.reminderTime.minute,
                        true,
                    ).show()
                },
            )
            Spacer(Modifier.height(12.dp))
            ErrorText(state.errorMessage)
            Spacer(Modifier.height(24.dp))
        }
        GradientButton(
            "Salvar hábito",
            onSave,
            icon = Icons.Rounded.Check,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun IconOption(icon: HabitIcon, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .then(if (selected) Modifier.shadow(10.dp, shape, ambientColor = Purple600, spotColor = Purple600) else Modifier)
            .clip(shape)
            .background(if (selected) Purple600 else Purple50)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon.vector, contentDescription = icon.name, tint = if (selected) Color.White else Purple600, modifier = Modifier.size(24.dp).habitIconRotation(icon))
    }
}

@Composable
private fun DayOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (selected) Purple600 else CardWhite)
            .border(if (selected) 0.dp else 1.5.dp, if (selected) Purple600 else BorderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else TextSecondary)
    }
}

@Preview(heightDp = 900)
@Composable
private fun HabitFormPreview() {
    HabitsTheme {
        HabitFormScreen(
            HabitFormUiState(
                name = "Beber água",
                description = "Manter o corpo hidratado durante o dia",
                frequency = HabitFrequency.CUSTOM,
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {},
        )
    }
}
