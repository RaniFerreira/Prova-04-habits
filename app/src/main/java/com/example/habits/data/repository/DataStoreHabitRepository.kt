package com.example.habits.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.habits.data.model.Habit
import com.example.habits.data.model.HabitCategory
import com.example.habits.data.model.HabitFrequency
import com.example.habits.data.model.HabitIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

private val Context.habitsDataStore: DataStore<Preferences> by preferencesDataStore(name = "habits")

/**
 * Persistência local com Jetpack DataStore (Preferences).
 *
 * Cada usuário tem sua própria chave ("habits_<uid>") contendo a lista de hábitos
 * serializada em JSON. O DataStore expõe um Flow, então qualquer alteração
 * (ex.: marcar um hábito como concluído) é propagada automaticamente para a UI.
 */
class DataStoreHabitRepository(context: Context) : HabitRepository {

    private val dataStore = context.applicationContext.habitsDataStore

    private fun keyFor(userId: String) = stringPreferencesKey("habits_$userId")

    override fun observeHabits(userId: String): Flow<List<Habit>> =
        dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { prefs -> decode(prefs[keyFor(userId)]).sortedWith(compareBy({ it.reminderTime }, { it.createdAt })) }
            .distinctUntilChanged()

    override fun observeHabit(userId: String, habitId: String): Flow<Habit?> =
        observeHabits(userId).map { habits -> habits.find { it.id == habitId } }.distinctUntilChanged()

    override suspend fun upsert(userId: String, habit: Habit) = update(userId) { habits ->
        if (habits.any { it.id == habit.id }) habits.map { if (it.id == habit.id) habit else it }
        else habits + habit
    }

    override suspend fun delete(userId: String, habitId: String) = update(userId) { habits ->
        habits.filterNot { it.id == habitId }
    }

    override suspend fun toggleCompletion(userId: String, habitId: String, date: LocalDate) =
        update(userId) { habits -> habits.map { if (it.id == habitId) it.toggle(date) else it } }

    private suspend fun update(userId: String, transform: (List<Habit>) -> List<Habit>) {
        dataStore.edit { prefs ->
            val key = keyFor(userId)
            prefs[key] = encode(transform(decode(prefs[key])))
        }
    }

    private fun encode(habits: List<Habit>): String = JSONArray().apply {
        habits.forEach { habit ->
            put(
                JSONObject()
                    .put("id", habit.id)
                    .put("name", habit.name)
                    .put("description", habit.description)
                    .put("goal", habit.goal)
                    .put("category", habit.category.name)
                    .put("icon", habit.icon.name)
                    .put("frequency", habit.frequency.name)
                    .put("days", JSONArray(habit.days.map { it.value }.sorted()))
                    .put("reminderTime", habit.reminderLabel)
                    .put("createdAt", habit.createdAt)
                    .put("completedDates", JSONArray(habit.completedDates.sorted()))
            )
        }
    }.toString()

    private fun decode(json: String?): List<Habit> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            List(array.length()) { index -> decodeHabit(array.getJSONObject(index)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun decodeHabit(obj: JSONObject): Habit {
        val dates = obj.optJSONArray("completedDates") ?: JSONArray()
        val days = obj.optJSONArray("days")
        return Habit(
            id = obj.getString("id"),
            name = obj.getString("name"),
            description = obj.optString("description"),
            goal = obj.optString("goal"),
            category = enumOrDefault(obj.optString("category"), HabitCategory.HEALTH),
            icon = enumOrDefault(obj.optString("icon"), HabitIcon.WATER),
            frequency = enumOrDefault(obj.optString("frequency"), HabitFrequency.DAILY),
            days = if (days == null) DayOfWeek.entries.toSet()
            else List(days.length()) { DayOfWeek.of(days.getInt(it)) }.toSet(),
            reminderTime = runCatching { LocalTime.parse(obj.optString("reminderTime")) }
                .getOrDefault(LocalTime.of(8, 0)),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            completedDates = List(dates.length()) { dates.getString(it) }.toSet(),
        )
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
        runCatching { enumValueOf<T>(name) }.getOrDefault(default)
}
