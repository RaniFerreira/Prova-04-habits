package com.example.habits.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.habits.data.model.HabitIcon
import com.example.habits.ui.theme.Amber
import com.example.habits.ui.theme.AmberSoft
import com.example.habits.ui.theme.BlueSoft
import com.example.habits.ui.theme.BorderColor
import com.example.habits.ui.theme.BrandGradient
import com.example.habits.ui.theme.CardWhite
import com.example.habits.ui.theme.Green
import com.example.habits.ui.theme.GreenSoft
import com.example.habits.ui.theme.GreenText
import com.example.habits.ui.theme.Orange
import com.example.habits.ui.theme.OrangeSoft
import com.example.habits.ui.theme.Pink
import com.example.habits.ui.theme.PinkSoft
import com.example.habits.ui.theme.Purple100
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.TextPlaceholder
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary

// ---------------------------------------------------------------- Ícones dos hábitos

val HabitIcon.vector: ImageVector
    get() = when (this) {
        HabitIcon.WATER -> Icons.Outlined.WaterDrop
        HabitIcon.BOOK -> Icons.AutoMirrored.Outlined.MenuBook
        HabitIcon.DUMBBELL -> Icons.Outlined.FitnessCenter
        HabitIcon.LEAF -> Icons.Outlined.Eco
        HabitIcon.MOON -> Icons.Outlined.DarkMode
        HabitIcon.FLAME -> Icons.Outlined.LocalFireDepartment
    }

/** O halter do Material é diagonal; no protótipo ele aparece na horizontal. */
fun Modifier.habitIconRotation(icon: HabitIcon): Modifier =
    if (icon == HabitIcon.DUMBBELL) this.rotate(-45f) else this

/** Cor de fundo e do ícone de cada tipo, como nos cards do protótipo. */
val HabitIcon.tileColors: Pair<Color, Color>
    get() = when (this) {
        HabitIcon.WATER -> BlueSoft to TextPrimary
        HabitIcon.BOOK -> Purple100 to Purple600
        HabitIcon.DUMBBELL -> PinkSoft to Pink
        HabitIcon.LEAF -> GreenSoft to Green
        HabitIcon.MOON -> AmberSoft to Amber
        HabitIcon.FLAME -> OrangeSoft to Orange
    }

@Composable
fun HabitIconTile(icon: HabitIcon, modifier: Modifier = Modifier, size: Dp = 46.dp) {
    val (background, tint) = icon.tileColors
    Box(
        modifier = modifier.size(size).background(background, RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon.vector, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f).habitIconRotation(icon))
    }
}

// ---------------------------------------------------------------- Campos

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = TextPrimary,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

/** Campo de texto do protótipo: fundo branco, borda suave e borda roxa quando focado. */
@Composable
fun HfTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    isError: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    var visible by rememberSaveable { mutableStateOf(false) }
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        focused -> Purple600
        else -> BorderColor
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        cursorBrush = SolidColor(Purple600),
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        interactionSource = interaction,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .background(CardWhite, RoundedCornerShape(16.dp))
                    .border(if (focused || isError) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            ) {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = if (focused) Purple600 else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(14.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = TextPlaceholder)
                    }
                    inner()
                }
                if (isPassword) {
                    Icon(
                        if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (visible) "Ocultar senha" else "Mostrar senha",
                        tint = TextSecondary,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            // Não recebe foco: o "Próximo" do teclado deve ir direto ao campo seguinte.
                            .focusProperties { canFocus = false }
                            .clickable { visible = !visible },
                    )
                }
            }
        },
    )
}

/** Campo somente leitura com aparência de input (ex.: horário do lembrete). */
@Composable
fun HfPickerField(text: String, leadingIcon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(leadingIcon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
    }
}

// ---------------------------------------------------------------- Botões

private val ButtonShape = RoundedCornerShape(18.dp)

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(14.dp, ButtonShape, ambientColor = Purple600, spotColor = Purple600)
            .clip(ButtonShape)
            .background(BrandGradient)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
        } else {
            ButtonContent(text, icon, Color.White)
        }
    }
}

@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(ButtonShape)
            .background(CardWhite)
            .border(BorderStroke(1.5.dp, Purple600), ButtonShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { ButtonContent(text, icon, Purple600) }
}

/** Botão de fundo suave (Editar, Excluir, Sair da conta). */
@Composable
fun SoftButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(ButtonShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { ButtonContent(text, icon, contentColor) }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/** Botão quadrado branco com sombra leve (voltar, filtros, notificações). */
@Composable
fun SquareIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = CardWhite,
        shadowElevation = 3.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun GradientFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .size(58.dp)
            .shadow(16.dp, shape, ambientColor = Purple600, spotColor = Purple600)
            .clip(shape)
            .background(BrandGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Add, contentDescription = "Novo hábito", tint = Color.White, modifier = Modifier.size(30.dp))
    }
}

// ---------------------------------------------------------------- Chips e tags

@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .background(if (selected) Purple600 else CardWhite)
            .border(if (selected) 0.dp else 1.5.dp, if (selected) Purple600 else BorderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null && selected) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
            color = if (selected) Color.White else TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
fun Tag(text: String, background: Color, content: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = MaterialTheme.typography.labelSmall, color = content, maxLines = 1)
    }
}

@Composable
fun StatusTag(done: Boolean, modifier: Modifier = Modifier) {
    if (done) Tag("Concluído", GreenSoft, GreenText, modifier) else Tag("Pendente", AmberSoft, Amber, modifier)
}

/** Círculo de status do dia (feito / não feito). */
@Composable
fun CheckToggle(checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (checked) Green else Color.Transparent)
            .border(if (checked) 0.dp else 1.5.dp, if (checked) Green else BorderColor, CircleShape)
            .clickable(onClickLabel = "Alternar status de hoje", onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Rounded.Check, contentDescription = "Concluído", tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

// ---------------------------------------------------------------- Card de hábito

data class HabitCardUi(
    val id: String,
    val name: String,
    val summary: String,
    val icon: HabitIcon,
    val isDone: Boolean,
)

@Composable
fun HabitCard(habit: HabitCardUi, onToggle: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            HabitIconTile(habit.icon)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        habit.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusTag(habit.isDone)
                }
            }
            Spacer(Modifier.width(10.dp))
            CheckToggle(habit.isDone, onToggle)
        }
    }
}

// ---------------------------------------------------------------- Cabeçalhos e fundos

/** Título centralizado com botão de voltar quadrado (Criar conta, Novo hábito). */
@Composable
fun BackTitleBar(title: String, onBack: () -> Unit, backIcon: ImageVector, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
        SquareIconButton(backIcon, "Voltar", onBack, Modifier.align(Alignment.CenterStart))
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
    }
}

/** Mancha lilás desfocada no canto superior direito das telas de login/cadastro. */
@Composable
fun LavenderGlow(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66B9A5FB), Color(0x00B9A5FB)),
                center = Offset(size.width * 0.95f, 0f),
                radius = size.width * 0.75f,
            ),
            radius = size.width * 0.75f,
            center = Offset(size.width * 0.95f, 0f),
        )
    }
}

/** Cabeçalho roxo com círculos decorativos (Detalhes e Perfil). */
@Composable
fun GradientHeader(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(BrandGradient),
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = 0.08f), radius = size.width * 0.42f, center = Offset(size.width * 0.95f, size.height * 0.15f))
            drawCircle(Color.White.copy(alpha = 0.07f), radius = size.width * 0.22f, center = Offset(0f, size.height * 0.85f))
        }
        content()
    }
}

/** Ajusta a cor dos ícones da barra de status (claros sobre o roxo, escuros sobre o fundo claro). */
@Composable
fun StatusBarIcons(dark: Boolean) {
    val activity = LocalActivity.current ?: return
    // Cada tela define a sua cor ao entrar; não restauramos ao sair para evitar conflito
    // durante a transição entre telas.
    DisposableEffect(dark) {
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars = dark
        onDispose { }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        if (action != null) {
            Text(
                action,
                style = MaterialTheme.typography.titleSmall,
                color = Purple600,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onAction).padding(4.dp),
            )
        }
    }
}

@Composable
fun SmallCaps(text: String, modifier: Modifier = Modifier, color: Color = TextSecondary) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall.merge(TextStyle(letterSpacing = MaterialTheme.typography.labelSmall.fontSize * 0.08f)),
        color = color,
        modifier = modifier,
    )
}

@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (message != null) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 20.dp),
        )
    }
}
