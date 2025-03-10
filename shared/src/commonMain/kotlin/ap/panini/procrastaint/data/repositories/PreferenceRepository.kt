package ap.panini.procrastaint.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
//        const val SHOW_INCOMPLETE = "show_incomplete"
//        const val SHOW_OLD = "show_old"
//
//        val boolPreferences = mapOf(
//            SHOW_COMPLETE to false,
//            SHOW_INCOMPLETE to true,
//            SHOW_OLD to false
//        )

        val stringPreference = mapOf(
            GOOGLE_REFRESH_TOKEN to "",
            GOOGLE_ACCESS_TOKEN to "",
            GOOGLE_CALENDAR_ID to "",
        )
    }

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
//    fun getBoolean(key: String): Flow<Boolean> = dataStore.data.map {
//        it[booleanPreferencesKey(key)] ?: boolPreferences[key]!!
//    }
//
//    suspend fun putBoolean(key: String, value: Boolean) {
//        dataStore.edit { it[booleanPreferencesKey(key)] = value }
//    }
}
