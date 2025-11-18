package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.ui.calendar.CalendarDisplayMode
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PreferenceRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val GOOGLE_REFRESH_TOKEN = "google_refresh_token"
        const val GOOGLE_ACCESS_TOKEN = "google_access_token"
        const val GOOGLE_CALENDAR_ID = "google_calendar_id"

        const val ON_BOARDING_COMPLETE = "landing_page_complete"
        const val CALENDAR_DISPLAY_MODE = "calendar_display_mode"
        const val SHOW_COMPLETED_TASKS = "show_completed_tasks"
        const val SHOW_INCOMPLETE_TASKS = "show_incomplete_tasks"

        val stringPreference = mapOf(
            GOOGLE_REFRESH_TOKEN to "",
            GOOGLE_ACCESS_TOKEN to "",
            GOOGLE_CALENDAR_ID to "",
            CALENDAR_DISPLAY_MODE to CalendarDisplayMode.DAILY.name
        )

        val boolPreference = mapOf(
            ON_BOARDING_COMPLETE to false,
            SHOW_COMPLETED_TASKS to true,
            SHOW_INCOMPLETE_TASKS to true
        )
    }

    /**
     * Get uuid unique to the user and app
     *
     * @return
     */
    @OptIn(ExperimentalUuidApi::class)
    fun getUuid(): String =
        runBlocking {
            val id = dataStore.data.map {
                it[stringPreferencesKey("UUID")]
            }.first()

            if (id != null) return@runBlocking id

            // generated a new uuid if there isn't one already
            val generatedId = Uuid.random().toHexString()
            dataStore.edit { it[stringPreferencesKey("UUID")] = generatedId }

            generatedId
        }

    fun getString(key: String): Flow<String> = dataStore.data.map {
        it[stringPreferencesKey(key)] ?: stringPreference[key]!!
    }

    suspend fun setString(key: String, value: String = stringPreference[key]!!) {
        dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    fun getBoolean(key: String): Flow<Boolean> = dataStore.data.map {
        it[booleanPreferencesKey(key)] ?: boolPreference[key]!!
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    fun getCalendarDisplayMode(): Flow<CalendarDisplayMode> = dataStore.data.map { preferences ->
        val modeName = preferences[stringPreferencesKey(CALENDAR_DISPLAY_MODE)] ?: CalendarDisplayMode.DAILY.name
        CalendarDisplayMode.valueOf(modeName)
    }

    suspend fun setCalendarDisplayMode(displayMode: CalendarDisplayMode) {
        dataStore.edit { settings ->
            settings[stringPreferencesKey(CALENDAR_DISPLAY_MODE)] = displayMode.name
        }
    }

    fun getShowCompletedTasks(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(SHOW_COMPLETED_TASKS)] ?: boolPreference[SHOW_COMPLETED_TASKS]!!
    }

    suspend fun setShowCompletedTasks(show: Boolean) {
        dataStore.edit { settings ->
            settings[booleanPreferencesKey(SHOW_COMPLETED_TASKS)] = show
        }
    }

    fun getShowIncompleteTasks(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(SHOW_INCOMPLETE_TASKS)] ?: boolPreference[SHOW_INCOMPLETE_TASKS]!!
    }

    suspend fun setShowIncompleteTasks(show: Boolean) {
        dataStore.edit { settings ->
            settings[booleanPreferencesKey(SHOW_INCOMPLETE_TASKS)] = show
        }
    }
}
