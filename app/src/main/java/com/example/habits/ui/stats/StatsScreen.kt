package com.example.habits.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.ui.components.SectionHeader
import com.example.habits.ui.components.StatusBarIcons
import com.example.habits.ui.components.Tag
import com.example.habits.ui.components.shortLabel
import com.example.habits.ui.theme.Amber
import com.example.habits.ui.theme.AmberSoft
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.BorderColor
import com.example.habits.ui.theme.BrandGradient
import com.example.habits.ui.theme.CardWhite
import com.example.habits.ui.theme.Green
import com.example.habits.ui.theme.GreenSoft
import com.example.habits.ui.theme.GreenText
import com.example.habits.ui.theme.HabitsTheme
import com.example.habits.ui.theme.Purple100
import com.example.habits.ui.theme.Purple400
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.Purple800
import com.example.habits.ui.theme.Red
import com.example.habits.ui.theme.RedSoft
import com.example.habits.ui.theme.TextPlaceholder
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary
import java.time.LocalDate

@Composable
fun StatsRoute(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(state)
}

@Composable
fun StatsScreen(state: StatsUiState) {
    StatusBarIcons(dark = true)
    var showAll by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Estatísticas", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                Text("Seu desempenho nesta semana", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Row(
                Modifier.height(44.dp).background(Purple600, RoundedCornerShape(50)).padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.CalendarToday, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Semana", style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(Icons.Outlined.TrackChanges, Purple100, Purple600, "${state.completionPercent}%", "Taxa de\nconclusão", Modifier.weight(1f))
            StatCard(Icons.Outlined.LocalFireDepartment, AmberSoft, TextPrimary, "${state.bestStreak}", "Dias\nseguidos", Modifier.weight(1f))
            StatCard(Icons.Rounded.Check, GreenSoft, Green, "${state.weekCheckIns}", "Check-ins\nna semana", Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        WeeklyChart(state)
        Spacer(Modifier.height(24.dp))
        SectionHeader("Conquistas", action = "Ver todas", onAction = { showAll = true })
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            state.featuredAchievements.forEach { AchievementBadge(it, Modifier.weight(1f)) }
        }
    }

    if (showAll) {
        AlertDialog(
            onDismissRequest = { showAll = false },
            containerColor = CardWhite,
            title = { Text("Conquistas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    state.achievements.forEach { achievement ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AchievementIconCircle(achievement, 44)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(achievement.title, style = MaterialTheme.typography.titleSmall, color = if (achievement.unlocked) TextPrimary else TextPlaceholder)
                                Text(achievement.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAll = false }) { Text("Fechar", color = Purple600) } },
        )
    }
}

@Composable
private fun StatCard(icon: ImageVector, iconBg: Color, iconTint: Color, value: String, label: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(24.dp), color = CardWhite, shadowElevation = 2.dp) {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.size(34.dp).background(iconBg, RoundedCornerShape(10.dp)), Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, minLines = 2)
        }
    }
}

@Composable
private fun WeeklyChart(state: StatsUiState) {
    Surface(shape = RoundedCornerShape(28.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("Conclusão semanal", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text(state.rangeLabel, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                state.deltaVsLastWeek?.let { delta ->
                    if (delta >= 0) Tag("+$delta%", GreenSoft, GreenText) else Tag("$delta%", RedSoft, Red)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                state.days.forEach { day -> Bar(day, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Bar(day: BarDay, modifier: Modifier) {
    val rate = day.rate
    Column(modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Text(
            if (rate == null) "–" else "${(rate * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = if (day.isToday) Purple600 else TextSecondary,
        )
        Spacer(Modifier.height(6.dp))
        val barHeight = if (rate == null) 8.dp else (16 + 104 * rate).dp
        val brush = when {
            rate == null -> Brush.linearGradient(listOf(BorderColor, BorderColor))
            day.isToday -> Brush.verticalGradient(listOf(Purple400, Purple600, Purple800))
            rate >= 0.99f -> Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFFA78BFA)))
            else -> Brush.linearGradient(listOf(Color(0xFFEAE5FD), Color(0xFFEAE5FD)))
        }
        Box(Modifier.width(26.dp).height(barHeight).clip(RoundedCornerShape(10.dp)).background(brush))
        Spacer(Modifier.height(8.dp))
        Text(
            day.date.dayOfWeek.shortLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = if (day.isToday) Purple600 else TextSecondary,
        )
    }
}

private val AchievementIcon.vector: ImageVector
    get() = when (this) {
        AchievementIcon.SPARKLE -> Icons.Outlined.AutoAwesome
        AchievementIcon.FLAME -> Icons.Outlined.LocalFireDepartment
        AchievementIcon.LEAF -> Icons.Outlined.Eco
        AchievementIcon.CALENDAR -> Icons.Outlined.CalendarMonth
        AchievementIcon.TROPHY -> Icons.Outlined.EmojiEvents
    }

@Composable
private fun AchievementIconCircle(achievement: Achievement, size: Int) {
    if (achievement.unlocked) {
        Box(
            Modifier
                .size(size.dp)
                .shadow(10.dp, CircleShape, ambientColor = Purple600, spotColor = Purple600)
                .clip(CircleShape)
                .background(BrandGradient),
            Alignment.Center,
        ) { Icon(achievement.icon.vector, null, tint = Color.White, modifier = Modifier.size((size * 0.42f).dp)) }
    } else {
        Box(Modifier.size(size.dp), Alignment.Center) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    BorderColor,
                    radius = this.size.minDimension / 2 - 1.dp.toPx(),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))),
                )
            }
            Icon(achievement.icon.vector, null, tint = TextPlaceholder, modifier = Modifier.size((size * 0.42f).dp))
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AchievementIconCircle(achievement, 60)
        Spacer(Modifier.height(10.dp))
        Text(
            achievement.title,
            style = MaterialTheme.typography.titleSmall,
            color = if (achievement.unlocked) TextPrimary else TextPlaceholder,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(heightDp = 900)
@Composable
private fun StatsPreview() {
    val today = LocalDate.of(2026, 11, 6)
    val rates = listOf(0.75f, 1f, 0.5f, 1f, 0.75f)
    HabitsTheme {
        StatsScreen(
            StatsUiState(
                completionPercent = 86, bestStreak = 12, weekCheckIns = 28, rangeLabel = "2 – 8 de novembro",
                deltaVsLastWeek = 12, isLoading = false,
                days = (0..6).map { i ->
                    val d = LocalDate.of(2026, 11, 2).plusDays(i.toLong())
                    BarDay(d, rates.getOrNull(i), d == today, d.isAfter(today))
                },
                achievements = listOf(
                    Achievement("Primeiro passo", "", AchievementIcon.SPARKLE, true),
                    Achievement("7 dias seguidos", "", AchievementIcon.FLAME, true),
                    Achievement("Mestre zen", "", AchievementIcon.LEAF, false),
                ),
            )
        )
    }
}
