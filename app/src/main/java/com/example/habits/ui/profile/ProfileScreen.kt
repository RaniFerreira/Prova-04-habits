package com.example.habits.ui.profile

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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.example.habits.ui.components.GradientHeader
import com.example.habits.ui.components.HfTextField
import com.example.habits.ui.components.SmallCaps
import com.example.habits.ui.components.SoftButton
import com.example.habits.ui.components.SquareIconButton
import com.example.habits.ui.components.StatusBarIcons
import com.example.habits.ui.components.Tag
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.BorderColor
import com.example.habits.ui.theme.CardWhite
import com.example.habits.ui.theme.HabitsTheme
import com.example.habits.ui.theme.Purple50
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.Red
import com.example.habits.ui.theme.RedSoft
import com.example.habits.ui.theme.TextPlaceholder
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary

@Composable
fun ProfileRoute(viewModel: ProfileViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.messageShown()
        }
    }
    ProfileScreen(
        state = state,
        onEditProfile = viewModel::startEditingName,
        onSaveName = viewModel::saveName,
        onCancelEdit = viewModel::cancelEditingName,
        onNotifications = { viewModel.showMessage("Lembretes diários ativos para todos os hábitos.") },
        onAppearance = { viewModel.showMessage("O HabitFlow usa o tema claro.") },
        onPrivacy = viewModel::sendPasswordReset,
        onSignOut = viewModel::signOut,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEditProfile: () -> Unit,
    onSaveName: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onNotifications: () -> Unit,
    onAppearance: () -> Unit,
    onPrivacy: () -> Unit,
    onSignOut: () -> Unit,
) {
    StatusBarIcons(dark = false)
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        Header(state, onEditProfile)
        Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            SmallCaps("Configurações")
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(26.dp), color = CardWhite, shadowElevation = 2.dp) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    SettingsRow(Icons.Outlined.Person, "Editar perfil", "Nome, foto e e-mail", onEditProfile)
                    SettingsRow(Icons.Outlined.Notifications, "Notificações", "Lembretes diários ativos", onNotifications)
                    SettingsRow(Icons.Outlined.DarkMode, "Aparência", "Tema claro", onAppearance)
                    SettingsRow(Icons.Outlined.Shield, "Privacidade e segurança", "Senha e dados da conta", onPrivacy)
                }
            }
            Spacer(Modifier.height(18.dp))
            SoftButton(
                "Sair da conta",
                Icons.AutoMirrored.Outlined.Logout,
                RedSoft,
                Red,
                { confirmSignOut = true },
                Modifier.fillMaxWidth(),
            )
        }
    }

    if (state.isEditingName) EditNameDialog(state, onSaveName, onCancelEdit)

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            containerColor = CardWhite,
            title = { Text("Sair da conta?") },
            text = { Text("Você precisará entrar novamente para ver seus hábitos.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("Sair", color = Red) } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancelar", color = TextSecondary) } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(state: ProfileUiState, onSettings: () -> Unit) {
    GradientHeader {
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                Text("Perfil", style = MaterialTheme.typography.titleLarge, color = Color.White)
                SquareIconButton(Icons.Outlined.Settings, "Configurações", onSettings, Modifier.align(Alignment.CenterEnd), tint = Purple600)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .size(104.dp)
                    .border(6.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .padding(6.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.initials, style = MaterialTheme.typography.headlineLarge, color = Purple600)
            }
            Spacer(Modifier.height(14.dp))
            Text(state.name, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(state.email, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val chip = Color.White.copy(alpha = 0.18f)
                Tag("${state.habitCount} ${if (state.habitCount == 1) "hábito" else "hábitos"}", chip, Color.White, Modifier.padding(vertical = 2.dp))
                Tag("${state.completionPercent}% de conclusão", chip, Color.White, Modifier.padding(vertical = 2.dp))
                Tag("${state.recordStreak} ${if (state.recordStreak == 1) "dia" else "dias"} recorde", chip, Color.White, Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).background(Purple50, RoundedCornerShape(14.dp)), Alignment.Center) {
            Icon(icon, null, tint = Purple600, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = TextPlaceholder)
    }
}

@Composable
private fun EditNameDialog(state: ProfileUiState, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(state.name) }
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = CardWhite,
        title = { Text("Editar perfil") },
        text = {
            Column {
                Text("Nome completo", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                HfTextField(name, { name = it }, "Seu nome", Icons.Outlined.Person)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))
                Text("E-mail: ${state.email}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }, enabled = !state.isSaving) {
                Text(if (state.isSaving) "Salvando..." else "Salvar", color = Purple600)
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar", color = TextSecondary) } },
    )
}

@Preview(heightDp = 900)
@Composable
private fun ProfilePreview() {
    HabitsTheme {
        ProfileScreen(
            ProfileUiState(
                name = "Ranielly Ferreira", email = "rani@email.com", initials = "RF",
                habitCount = 5, completionPercent = 86, recordStreak = 21,
            ),
            {}, {}, {}, {}, {}, {}, {},
        )
    }
}
