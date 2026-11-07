package com.example.habits.data.repository

import com.example.habits.data.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

/** Contrato da autenticação. A View/ViewModel não conhecem o Firebase diretamente. */
interface AuthRepository {
    /** Usuário logado (ou null). Estado observável usado para decidir a navegação. */
    val currentUser: StateFlow<AuthUser?>

    /** false quando o app foi compilado sem o google-services.json. */
    val isConfigured: Boolean

    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun register(name: String, email: String, password: String): Result<AuthUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun updateDisplayName(name: String): Result<AuthUser>
    fun signOut()
}

/** Erro de autenticação com mensagem já traduzida para exibir ao usuário. */
class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
