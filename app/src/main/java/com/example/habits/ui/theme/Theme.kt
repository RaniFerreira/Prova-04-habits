package com.example.habits.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val HabitFlowColors = lightColorScheme(
    primary = Purple600,
    onPrimary = Color.White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple600,
    secondary = Green,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    surfaceVariant = Purple50,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = CardWhite,
    outline = BorderColor,
    outlineVariant = BorderColor,
    error = Red,
)

private val HabitFlowShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/** Tema claro do protótipo ("Aparência: Tema claro"). */
@Composable
fun HabitsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HabitFlowColors,
        typography = Typography,
        shapes = HabitFlowShapes,
        content = content,
    )
}
