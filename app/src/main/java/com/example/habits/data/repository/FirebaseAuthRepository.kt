package com.example.habits.data.repository

import android.content.Context
import android.util.Log
import com.example.habits.data.model.AuthUser
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementação de [AuthRepository] com Firebase Authentication (e-mail/senha).
 *
 * Um AuthStateListener mantém [currentUser] sincronizado com a sessão do Firebase,
 * que persiste o login entre execuções do app.
 */
class FirebaseAuthRepository(context: Context) : AuthRepository {

    private val auth: FirebaseAuth? = try {
        if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context)
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w(TAG, "Firebase não configurado: adicione app/google-services.json", e)
        null
    }

    private val _currentUser = MutableStateFlow(auth?.currentUser?.toAuthUser())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override val isConfigured: Boolean = auth != null

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser?.toAuthUser()
        }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> =
        runAuth { auth ->
            val result = auth.signInWithEmailAndPassword(email, password).await()
            requireNotNull(result.user).toAuthUser()
        }

    override suspend fun register(name: String, email: String, password: String): Result<AuthUser> =
        runAuth { auth ->
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = requireNotNull(result.user)
            val profile = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            user.updateProfile(profile).await()
            // O listener disparou antes de o nome ser salvo; publica o usuário atualizado.
            user.toAuthUser().copy(displayName = name).also { _currentUser.value = it }
        }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runAuth { auth -> auth.sendPasswordResetEmail(email).await(); Unit }

    override suspend fun updateDisplayName(name: String): Result<AuthUser> =
        runAuth { auth ->
            val user = auth.currentUser ?: throw AuthException("Sessão expirada. Entre novamente.")
            val profile = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            user.updateProfile(profile).await()
            user.toAuthUser().copy(displayName = name).also { _currentUser.value = it }
        }

    override fun signOut() {
        auth?.signOut()
    }

    private suspend fun <T> runAuth(block: suspend (FirebaseAuth) -> T): Result<T> {
        val auth = auth ?: return Result.failure(AuthException(NOT_CONFIGURED_MESSAGE))
        return try {
            Result.success(block(auth))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falha na autenticação", e)
            Result.failure(AuthException(e.toFriendlyMessage(), e))
        }
    }

    private fun Exception.toFriendlyMessage(): String = when {
        this is AuthException -> message.orEmpty()
        // Projeto sem Authentication iniciado ou sem o provedor E-mail/senha ativado.
        message.orEmpty().contains("CONFIGURATION_NOT_FOUND") ||
            message.orEmpty().contains("OPERATION_NOT_ALLOWED") ->
            "Login por e-mail/senha não está ativado no Firebase (Authentication → Sign-in method)."
        else -> toFirebaseMessage()
    }

    private fun Exception.toFirebaseMessage(): String = when (this) {
        is FirebaseAuthWeakPasswordException -> "Senha fraca. Use pelo menos 6 caracteres."
        is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."
        is FirebaseAuthInvalidUserException -> "Usuário não encontrado ou desativado."
        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos."
        is FirebaseNetworkException -> "Sem conexão com a internet. Tente novamente."
        is FirebaseTooManyRequestsException -> "Muitas tentativas. Aguarde um pouco e tente de novo."
        else -> localizedMessage ?: "Não foi possível concluir a operação."
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(uid = uid, email = email, displayName = displayName)

    companion object {
        private const val TAG = "FirebaseAuthRepository"
        const val NOT_CONFIGURED_MESSAGE =
            "Firebase não configurado. Adicione o arquivo app/google-services.json e recompile."
    }
}
