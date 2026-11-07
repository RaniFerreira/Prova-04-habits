package com.example.habits

import android.app.Application
import android.content.Context
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.CredentialStore
import com.example.habits.data.repository.DataStoreHabitRepository
import com.example.habits.data.repository.FirebaseAuthRepository
import com.example.habits.data.repository.HabitRepository

/** Injeção de dependências manual: uma instância de cada repositório para o app todo. */
class AppContainer(context: Context) {
    val authRepository: AuthRepository = FirebaseAuthRepository(context)
    val habitRepository: HabitRepository = DataStoreHabitRepository(context)
    val credentialStore = CredentialStore(context)
}

class HabitsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
