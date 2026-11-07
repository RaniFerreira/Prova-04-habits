package com.example.habits.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.habits.R

/** Poppins, a fonte usada no protótipo. */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int = (size * 1.4).toInt()) =
    TextStyle(fontFamily = Poppins, fontWeight = weight, fontSize = size.sp, lineHeight = lineHeight.sp)

val Typography = Typography(
    headlineLarge = style(28, FontWeight.Bold, 36),     // "Meus hábitos", "Olá, Rani!"
    headlineMedium = style(26, FontWeight.Bold, 34),    // "Bem-vindo de volta!"
    headlineSmall = style(22, FontWeight.SemiBold, 30), // "Hábitos de hoje"
    titleLarge = style(18, FontWeight.SemiBold, 26),
    titleMedium = style(16, FontWeight.SemiBold, 22),   // título do card
    titleSmall = style(15, FontWeight.Medium, 20),
    bodyLarge = style(15, FontWeight.Normal, 22),
    bodyMedium = style(13, FontWeight.Normal, 19),
    bodySmall = style(12, FontWeight.Normal, 16),
    labelLarge = style(16, FontWeight.SemiBold, 22),    // botões
    labelMedium = style(13, FontWeight.Medium, 18),     // rótulos dos campos
    labelSmall = style(12, FontWeight.Medium, 16),      // tags
)
