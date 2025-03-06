package ap.panini.procrastaint.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val GOOGLE_REFRESH_TOKEN = "google_refresh_token"
        const val GOOGLE_ACCESS_TOKEN = "google_access_token"
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
            GOOGLE_ACCESS_TOKEN to ""
        )
    }

    fun getString(key: String): Flow<String> = dataStore.data.map {
        it[stringPreferencesKey(key)] ?: stringPreference[key]!!
    }

    fun getStringOrNull(key: String): Flow<String?> = dataStore.data.map {
        it[stringPreferencesKey(key)]
    }

    suspend fun setString(key: String, value: String) {
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
