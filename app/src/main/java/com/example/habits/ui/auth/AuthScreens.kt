package com.example.habits.ui.auth

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habits.ui.components.BackTitleBar
import com.example.habits.ui.components.ErrorText
import com.example.habits.ui.components.FieldLabel
import com.example.habits.ui.components.GradientButton
import com.example.habits.ui.components.HfTextField
import com.example.habits.ui.components.LavenderGlow
import com.example.habits.ui.components.OutlineButton
import com.example.habits.ui.components.StatusBarIcons
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.BorderColor
import com.example.habits.ui.theme.BrandGradient
import com.example.habits.ui.theme.HabitsTheme
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary

// ======================================================== Rotas (View <-> ViewModel)

@Composable
fun LoginRoute(viewModel: AuthViewModel, onNavigateToRegister: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? FragmentActivity

    // Evento do ViewModel: abrir o BiometricPrompt.
    LaunchedEffect(state.requestBiometricPrompt) {
        if (!state.requestBiometricPrompt) return@LaunchedEffect
        viewModel.onBiometricPromptShown()
        if (activity == null) return@LaunchedEffect
        val canAuth = BiometricManager.from(activity).canAuthenticate(BIOMETRIC_WEAK)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            viewModel.onBiometricError("Nenhuma biometria cadastrada neste aparelho.")
            return@LaunchedEffect
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                    viewModel.onBiometricSuccess()

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED
                    ) viewModel.onBiometricError(errString.toString())
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Entrar no HabitFlow")
                .setSubtitle("Use sua biometria para continuar")
                .setNegativeButtonText("Cancelar")
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .build()
        )
    }

    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::signIn,
        onForgotPassword = viewModel::sendPasswordReset,
        onBiometric = viewModel::onBiometricClick,
        onNavigateToRegister = onNavigateToRegister,
    )
}

@Composable
fun RegisterRoute(viewModel: AuthViewModel, onNavigateToLogin: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onAcceptTermsChange = viewModel::onAcceptTermsChange,
        onRegister = viewModel::register,
        onNavigateToLogin = onNavigateToLogin,
    )
}

// ======================================================== Telas

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onBiometric: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    AuthBackground {
        Spacer(Modifier.height(36.dp))
        AppLogo()
        Spacer(Modifier.height(10.dp))
        Text("HabitFlow", style = MaterialTheme.typography.titleSmall, color = Purple600, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Text(
            "Bem-vindo de volta!",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Entre para continuar construindo hábitos que transformam sua rotina.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        )
        Spacer(Modifier.height(32.dp))

        FieldLabel("E-mail", Modifier.fillMaxWidth())
        HfTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "seu@email.com",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(18.dp))
        FieldLabel("Senha", Modifier.fillMaxWidth())
        HfTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = "Digite sua senha",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        )
        Text(
            "Esqueceu a senha?",
            style = MaterialTheme.typography.titleSmall,
            color = Purple600,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onForgotPassword)
                .padding(4.dp),
        )
        Spacer(Modifier.height(12.dp))
        AuthMessages(state)
        Spacer(Modifier.height(12.dp))
        GradientButton("Entrar", onSignIn, isLoading = state.isLoading)
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = BorderColor)
            Text("ou", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(horizontal = 18.dp))
            HorizontalDivider(Modifier.weight(1f), color = BorderColor)
        }
        Spacer(Modifier.height(20.dp))
        OutlineButton("Entrar com Biometria", onBiometric, icon = Icons.Outlined.Shield)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        FooterLink("Não tem uma conta? ", "Cadastre-se", onNavigateToRegister)
    }
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAcceptTermsChange: (Boolean) -> Unit,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    AuthBackground {
        BackTitleBar("Criar conta", onNavigateToLogin, Icons.AutoMirrored.Rounded.ArrowBack)
        Spacer(Modifier.height(20.dp))
        Text("Comece sua jornada", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        Text(
            "Crie sua conta para organizar sua rotina e acompanhar sua evolução.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))

        FieldLabel("Nome completo", Modifier.fillMaxWidth())
        HfTextField(state.name, onNameChange, "Seu nome", Icons.Outlined.Person)
        Spacer(Modifier.height(16.dp))
        FieldLabel("E-mail", Modifier.fillMaxWidth())
        HfTextField(state.email, onEmailChange, "seu@email.com", Icons.Outlined.Email, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(16.dp))
        FieldLabel("Senha", Modifier.fillMaxWidth())
        HfTextField(state.password, onPasswordChange, "Mínimo de 6 caracteres", Icons.Outlined.Lock, isPassword = true, keyboardType = KeyboardType.Password)
        Spacer(Modifier.height(16.dp))
        FieldLabel("Confirmar senha", Modifier.fillMaxWidth())
        HfTextField(
            state.confirmPassword,
            onConfirmPasswordChange,
            "Repita a senha",
            Icons.Outlined.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(18.dp))
        TermsCheckbox(state.acceptedTerms, onAcceptTermsChange)
        Spacer(Modifier.height(12.dp))
        AuthMessages(state)
        Spacer(Modifier.height(12.dp))
        GradientButton("Criar conta", onRegister, isLoading = state.isLoading)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        FooterLink("Já tem uma conta? ", "Entrar", onNavigateToLogin)
    }
}

// ======================================================== Partes

@Composable
private fun AuthBackground(content: @Composable ColumnScope.() -> Unit) {
    StatusBarIcons(dark = true)
    Box(Modifier.fillMaxSize().background(AppBackground)) {
        LavenderGlow()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

/** Logo do HabitFlow: quadrado roxo em gradiente com um check e um ponto. */
@Composable
private fun AppLogo() {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = Modifier
            .size(76.dp)
            .shadow(12.dp, shape, ambientColor = Purple600, spotColor = Purple600)
            .clip(shape)
            .background(BrandGradient),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
        Box(
            Modifier
                .align(Alignment.Center)
                .padding(start = 38.dp, top = 30.dp)
                .size(9.dp)
                .background(Color.White, CircleShape)
        )
    }
}

@Composable
private fun TermsCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) Purple600 else Color.White)
                .border(1.5.dp, if (checked) Purple600 else BorderColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "Li e aceito os Termos de uso e a Política de privacidade.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
private fun AuthMessages(state: AuthUiState) {
    ErrorText(state.errorMessage)
    state.infoMessage?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = Purple600, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun FooterLink(question: String, action: String, onClick: () -> Unit) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = TextSecondary)) { append(question) }
            withStyle(SpanStyle(color = Purple600, fontWeight = FontWeight.SemiBold)) { append(action) }
        },
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginPreview() {
    HabitsTheme { LoginScreen(AuthUiState(email = "rani@email.com"), {}, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RegisterPreview() {
    HabitsTheme {
        RegisterScreen(AuthUiState(name = "Ranielly Ferreira", acceptedTerms = true), {}, {}, {}, {}, {}, {}, {})
    }
}
