package ap.panini.procrastaint.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val SHOW_COMPLETE = "show_complete"
        const val SHOW_INCOMPLETE = "show_incomplete"
        const val SHOW_OLD = "show_old"

        val boolPreferences = mapOf(
            SHOW_COMPLETE to false,
            SHOW_INCOMPLETE to true,
            SHOW_OLD to false
        )
    }

    fun getBoolean(key: String): Flow<Boolean> = dataStore.data.map {
        it[booleanPreferencesKey(key)] ?: boolPreferences[key]!!
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }
}
