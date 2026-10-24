package ap.panini.procrastaint.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import ap.panini.kwhen.configs.ENConfig
import ap.panini.procrastaint.data.models.DateFormatMode
import ap.panini.procrastaint.data.models.ResolvedDateTimeFormat
import ap.panini.procrastaint.data.models.TimeFormatMode
import ap.panini.procrastaint.ui.calendar.CalendarDisplayMode
import ap.panini.procrastaint.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Suppress("TooManyFunctions")
class PreferenceRepository(
    private val dataStore: DataStore<Preferences>,
    private val systemDateTimeProvider: SystemDateTimeProvider = SystemDateTimeProvider()
) {
    companion object {
        const val GOOGLE_REFRESH_TOKEN = "google_refresh_token"
        const val GOOGLE_ACCESS_TOKEN = "google_access_token"
        const val GOOGLE_CALENDAR_ID = "google_calendar_id"

        const val ON_BOARDING_COMPLETE = "landing_page_complete"
        const val CALENDAR_DISPLAY_MODE = "calendar_display_mode"
        const val SHOW_COMPLETED_TASKS = "show_completed_tasks"
        const val SHOW_INCOMPLETE_TASKS = "show_incomplete_tasks"
        const val SHOW_PROCRASTAINT_TASKS = "show_procrastaint_tasks"
        const val SHOW_DEVICE_CALENDAR = "show_device_calendar"
        const val SHOW_GOOGLE_CALENDAR = "show_google_calendar"

        const val TIME_FORMAT_MODE = "time_format_mode"
        const val DATE_FORMAT_MODE = "date_format_mode"

        const val KWHEN_MORNING_HOUR = "kwhen_morning_hour"
        const val KWHEN_AFTERNOON_HOUR = "kwhen_afternoon_hour"
        const val KWHEN_EVENING_HOUR = "kwhen_evening_hour"
        const val KWHEN_NIGHT_HOUR = "kwhen_night_hour"
        const val KWHEN_USE_24 = "kwhen_use_24"
        const val KWHEN_USE_EUROPEAN_DATE = "kwhen_use_european_date"

        val stringPreference = mapOf(
            GOOGLE_REFRESH_TOKEN to "",
            GOOGLE_ACCESS_TOKEN to "",
            GOOGLE_CALENDAR_ID to "",
            CALENDAR_DISPLAY_MODE to CalendarDisplayMode.DAILY.name,
            TIME_FORMAT_MODE to TimeFormatMode.SYSTEM.name,
            DATE_FORMAT_MODE to DateFormatMode.SYSTEM.name,
        )

        val intPreference = mapOf(
            KWHEN_MORNING_HOUR to 9,
            KWHEN_AFTERNOON_HOUR to 15,
            KWHEN_EVENING_HOUR to 18,
            KWHEN_NIGHT_HOUR to 20,
        )

        val boolPreference = mapOf(
            ON_BOARDING_COMPLETE to false,
            SHOW_COMPLETED_TASKS to true,
            SHOW_INCOMPLETE_TASKS to true,
            SHOW_PROCRASTAINT_TASKS to true,
            SHOW_DEVICE_CALENDAR to true,
            SHOW_GOOGLE_CALENDAR to true,
            KWHEN_USE_24 to false,
            KWHEN_USE_EUROPEAN_DATE to false,
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

    fun getShowProcrastaintTasks(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(SHOW_PROCRASTAINT_TASKS)] ?: boolPreference[SHOW_PROCRASTAINT_TASKS]!!
    }

    suspend fun setShowProcrastaintTasks(show: Boolean) {
        dataStore.edit { settings ->
            settings[booleanPreferencesKey(SHOW_PROCRASTAINT_TASKS)] = show
        }
    }

    fun getShowDeviceCalendar(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(SHOW_DEVICE_CALENDAR)] ?: boolPreference[SHOW_DEVICE_CALENDAR]!!
    }

    suspend fun setShowDeviceCalendar(show: Boolean) {
        dataStore.edit { settings ->
            settings[booleanPreferencesKey(SHOW_DEVICE_CALENDAR)] = show
        }
    }

    fun getShowGoogleCalendar(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey(SHOW_GOOGLE_CALENDAR)] ?: boolPreference[SHOW_GOOGLE_CALENDAR]!!
    }

    suspend fun setShowGoogleCalendar(show: Boolean) {
        dataStore.edit { settings ->
            settings[booleanPreferencesKey(SHOW_GOOGLE_CALENDAR)] = show
        }
    }

    fun getTimeFormatMode(): Flow<TimeFormatMode> = dataStore.data.map { preferences ->
        extractTimeFormatMode(preferences)
    }

    fun getDateFormatMode(): Flow<DateFormatMode> = dataStore.data.map { preferences ->
        extractDateFormatMode(preferences)
    }

    suspend fun setTimeFormatMode(mode: TimeFormatMode) {
        dataStore.edit { settings ->
            settings[stringPreferencesKey(TIME_FORMAT_MODE)] = mode.name
        }
    }

    suspend fun setDateFormatMode(mode: DateFormatMode) {
        dataStore.edit { settings ->
            settings[stringPreferencesKey(DATE_FORMAT_MODE)] = mode.name
        }
    }

    fun getResolvedDateTimeFormat(): Flow<ResolvedDateTimeFormat> = dataStore.data.map { preferences ->
        ResolvedDateTimeFormat(
            use24Hour = resolveTimeFormat(preferences),
            useEuropeanDate = resolveDateFormat(preferences)
        )
    }

    fun getKwhenConfig(): Flow<ENConfig> = dataStore.data.map { preferences ->
        ENConfig(
            morning = preferences[intPreferencesKey(KWHEN_MORNING_HOUR)]
                ?: intPreference[KWHEN_MORNING_HOUR]!!,
            afternoon = preferences[intPreferencesKey(KWHEN_AFTERNOON_HOUR)]
                ?: intPreference[KWHEN_AFTERNOON_HOUR]!!,
            evening = preferences[intPreferencesKey(KWHEN_EVENING_HOUR)]
                ?: intPreference[KWHEN_EVENING_HOUR]!!,
            night = preferences[intPreferencesKey(KWHEN_NIGHT_HOUR)]
                ?: intPreference[KWHEN_NIGHT_HOUR]!!,
            use24 = resolveTimeFormat(preferences),
            useEuropeanDate = resolveDateFormat(preferences)
        )
    }

    private fun extractTimeFormatMode(preferences: Preferences): TimeFormatMode {
        val modeName = preferences[stringPreferencesKey(TIME_FORMAT_MODE)]
        return if (modeName != null) {
            runCatching { TimeFormatMode.valueOf(modeName) }.getOrDefault(TimeFormatMode.SYSTEM)
        } else {
            val legacy24 = preferences[booleanPreferencesKey(KWHEN_USE_24)]
            if (legacy24 == true) TimeFormatMode.H24 else TimeFormatMode.SYSTEM
        }
    }

    private fun extractDateFormatMode(preferences: Preferences): DateFormatMode {
        val modeName = preferences[stringPreferencesKey(DATE_FORMAT_MODE)]
        return if (modeName != null) {
            runCatching { DateFormatMode.valueOf(modeName) }.getOrDefault(DateFormatMode.SYSTEM)
        } else {
            val legacyEuropean = preferences[booleanPreferencesKey(KWHEN_USE_EUROPEAN_DATE)]
            if (legacyEuropean == true) DateFormatMode.DMY else DateFormatMode.SYSTEM
        }
    }

    private fun resolveTimeFormat(preferences: Preferences): Boolean =
        when (extractTimeFormatMode(preferences)) {
            TimeFormatMode.H24 -> true
            TimeFormatMode.H12 -> false
            TimeFormatMode.SYSTEM -> systemDateTimeProvider.is24HourFormat()
        }

    private fun resolveDateFormat(preferences: Preferences): Boolean =
        when (extractDateFormatMode(preferences)) {
            DateFormatMode.DMY -> true
            DateFormatMode.MDY -> false
            DateFormatMode.SYSTEM -> systemDateTimeProvider.isEuropeanDateFormat()
        }

    suspend fun setMorningHour(hour: Int) {
        dataStore.edit { settings ->
            settings[intPreferencesKey(KWHEN_MORNING_HOUR)] = hour
        }
    }

    suspend fun setAfternoonHour(hour: Int) {
        dataStore.edit { settings ->
            settings[intPreferencesKey(KWHEN_AFTERNOON_HOUR)] = hour
        }
    }

    suspend fun setEveningHour(hour: Int) {
        dataStore.edit { settings ->
            settings[intPreferencesKey(KWHEN_EVENING_HOUR)] = hour
        }
    }

    suspend fun setNightHour(hour: Int) {
        dataStore.edit { settings ->
            settings[intPreferencesKey(KWHEN_NIGHT_HOUR)] = hour
        }
    }

    suspend fun setUse24Hour(use24: Boolean) {
        setTimeFormatMode(if (use24) TimeFormatMode.H24 else TimeFormatMode.H12)
        dataStore.edit { settings ->
            settings[booleanPreferencesKey(KWHEN_USE_24)] = use24
        }
    }

    suspend fun setUseEuropeanDate(useEuropeanDate: Boolean) {
        setDateFormatMode(if (useEuropeanDate) DateFormatMode.DMY else DateFormatMode.MDY)
        dataStore.edit { settings ->
            settings[booleanPreferencesKey(KWHEN_USE_EUROPEAN_DATE)] = useEuropeanDate
        }
    }

    suspend fun resetKwhenConfigToDefaults() {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(KWHEN_MORNING_HOUR)] = intPreference[KWHEN_MORNING_HOUR]!!
            preferences[intPreferencesKey(KWHEN_AFTERNOON_HOUR)] = intPreference[KWHEN_AFTERNOON_HOUR]!!
            preferences[intPreferencesKey(KWHEN_EVENING_HOUR)] = intPreference[KWHEN_EVENING_HOUR]!!
            preferences[intPreferencesKey(KWHEN_NIGHT_HOUR)] = intPreference[KWHEN_NIGHT_HOUR]!!
            preferences[booleanPreferencesKey(KWHEN_USE_24)] = boolPreference[KWHEN_USE_24]!!
            preferences[booleanPreferencesKey(KWHEN_USE_EUROPEAN_DATE)] = boolPreference[KWHEN_USE_EUROPEAN_DATE]!!
            preferences[stringPreferencesKey(TIME_FORMAT_MODE)] = stringPreference[TIME_FORMAT_MODE]!!
            preferences[stringPreferencesKey(DATE_FORMAT_MODE)] = stringPreference[DATE_FORMAT_MODE]!!
        }
    }
}
