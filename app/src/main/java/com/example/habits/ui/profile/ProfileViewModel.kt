package com.example.habits.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habits.AppContainer
import com.example.habits.data.model.HabitStats
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val initials: String = "",
    val habitCount: Int = 0,
    val completionPercent: Int = 0,
    val recordStreak: Int = 0,
    val isEditingName: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
)

private data class ProfileUiEvents(
    val isEditingName: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
)

/** ViewModel do Perfil: dados do usuário (Firebase), resumo dos hábitos e ações da conta. */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    habitRepository: HabitRepository,
    userId: String,
) : ViewModel() {

    private val events = MutableStateFlow(ProfileUiEvents())

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.currentUser,
        habitRepository.observeHabits(userId),
        events,
    ) { user, habits, ev ->
        val today = LocalDate.now()
        ProfileUiState(
            name = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.firstName.orEmpty(),
            email = user?.email.orEmpty(),
            initials = user?.initials.orEmpty(),
            habitCount = habits.size,
            completionPercent = HabitStats.percent(HabitStats.overallRate(habits, today)),
            recordStreak = habits.maxOfOrNull { it.longestStreak(today) } ?: 0,
            isEditingName = ev.isEditingName,
            isSaving = ev.isSaving,
            message = ev.message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun startEditingName() = events.update { it.copy(isEditingName = true) }
    fun cancelEditingName() = events.update { it.copy(isEditingName = false) }

    fun saveName(name: String) {
        if (name.isBlank()) return events.update { it.copy(message = "O nome não pode ficar vazio.") }
        events.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = authRepository.updateDisplayName(name.trim())
            events.update {
                it.copy(
                    isSaving = false,
                    isEditingName = result.isFailure,
                    message = result.exceptionOrNull()?.message ?: "Perfil atualizado.",
                )
            }
        }
    }

    fun sendPasswordReset() {
        val email = uiState.value.email
        if (email.isBlank()) return
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            showMessage(result.exceptionOrNull()?.message ?: "Enviamos um link para redefinir sua senha em $email.")
        }
    }

    fun showMessage(message: String) = events.update { it.copy(message = message) }
    fun messageShown() = events.update { it.copy(message = null) }

    fun signOut() = authRepository.signOut()

    companion object {
        fun factory(container: AppContainer, userId: String) = viewModelFactory {
            initializer { ProfileViewModel(container.authRepository, container.habitRepository, userId) }
        }
    }
}
