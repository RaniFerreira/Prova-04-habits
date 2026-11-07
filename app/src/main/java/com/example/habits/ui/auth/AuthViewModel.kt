package com.example.habits.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habits.AppContainer
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.CredentialStore
import com.example.habits.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado imutável das telas de login e cadastro. */
data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    /** Pede para a View abrir o BiometricPrompt (evento consumido pela View). */
    val requestBiometricPrompt: Boolean = false,
)

/**
 * ViewModel da autenticação. Recebe eventos da View, valida, chama o [AuthRepository]
 * e publica o resultado em [uiState].
 *
 * Após login/cadastro com sucesso não há navegação explícita aqui: o
 * AuthRepository.currentUser muda e o app troca automaticamente para o gerenciador de hábitos.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val credentialStore: CredentialStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            errorMessage = if (authRepository.isConfigured) null
            else FirebaseAuthRepository.NOT_CONFIGURED_MESSAGE
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun onAcceptTermsChange(value: Boolean) = _uiState.update { it.copy(acceptedTerms = value, errorMessage = null) }

    fun signIn() {
        val state = _uiState.value
        AuthValidator.validateLogin(state.email, state.password)?.let { return showError(it) }
        val email = state.email.trim()
        execute(onSuccess = { credentialStore.save(email, state.password) }) {
            authRepository.signIn(email, state.password).map { }
        }
    }

    fun register() {
        val state = _uiState.value
        AuthValidator.validateRegister(state.name, state.email, state.password, state.confirmPassword, state.acceptedTerms)
            ?.let { return showError(it) }
        val email = state.email.trim()
        execute(onSuccess = { credentialStore.save(email, state.password) }) {
            authRepository.register(state.name.trim(), email, state.password).map { }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email.trim()
        if (!AuthValidator.isValidEmail(email)) {
            return showError("Digite seu e-mail acima para redefinir a senha.")
        }
        execute(successMessage = "Enviamos um link de redefinição para $email.") {
            authRepository.sendPasswordReset(email)
        }
    }

    // ---- Biometria

    fun onBiometricClick() {
        if (!credentialStore.hasCredentials()) {
            return showError("Entre uma vez com e-mail e senha para ativar o login por biometria.")
        }
        _uiState.update { it.copy(requestBiometricPrompt = true, errorMessage = null) }
    }

    fun onBiometricPromptShown() = _uiState.update { it.copy(requestBiometricPrompt = false) }

    fun onBiometricSuccess() {
        val (email, password) = credentialStore.load()
            ?: return showError("Não foi possível recuperar seu login. Entre com e-mail e senha.")
        execute(onFailure = { credentialStore.clear() }) { authRepository.signIn(email, password).map { } }
    }

    fun onBiometricError(message: String) = showError(message)

    private fun showError(message: String) =
        _uiState.update { it.copy(errorMessage = message, infoMessage = null) }

    private fun execute(
        successMessage: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
        action: suspend () -> Result<Unit>,
    ) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            val result = action()
            if (result.isSuccess) onSuccess() else onFailure()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message,
                    infoMessage = if (result.isSuccess) successMessage else null,
                )
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { AuthViewModel(container.authRepository, container.credentialStore) }
        }
    }
}

/** Regras de validação dos formulários (puras, testáveis sem Android). */
object AuthValidator {
    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    const val MIN_PASSWORD_LENGTH = 6

    fun isValidEmail(email: String) = emailRegex.matches(email.trim())

    fun validateLogin(email: String, password: String): String? = when {
        !isValidEmail(email) -> "Informe um e-mail válido."
        password.isEmpty() -> "Informe sua senha."
        else -> null
    }

    fun validateRegister(
        name: String,
        email: String,
        password: String,
        confirm: String,
        acceptedTerms: Boolean,
    ): String? = when {
        name.isBlank() -> "Informe seu nome completo."
        !isValidEmail(email) -> "Informe um e-mail válido."
        password.length < MIN_PASSWORD_LENGTH -> "A senha deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres."
        password != confirm -> "As senhas não coincidem."
        !acceptedTerms -> "Aceite os Termos de uso e a Política de privacidade para continuar."
        else -> null
    }
}
