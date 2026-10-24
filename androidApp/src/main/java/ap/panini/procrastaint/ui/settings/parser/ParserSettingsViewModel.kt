package ap.panini.procrastaint.ui.settings.parser

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.kwhen.TimeParser
import ap.panini.kwhen.configs.ENConfig
import ap.panini.procrastaint.data.models.DateFormatMode
import ap.panini.procrastaint.data.models.ResolvedDateTimeFormat
import ap.panini.procrastaint.data.models.TimeFormatMode
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val NoonHour = 12
private const val PadLength = 2

@Immutable
data class ComparisonDiffItem(
    val label: String,
    val input: String,
    val previousOutput: String,
    val currentOutput: String,
    val hasChanged: Boolean
)

private data class PresetPhrase(val label: String, val input: String)

private val PresetPhrases = listOf(
    PresetPhrase("Morning Keyword", "Gym tomorrow morning"),
    PresetPhrase("Date & Ambiguous Hour", "Doctor 04/05 at 8"),
    PresetPhrase("24-Hour & Recurrence", "Meeting every Tuesday at 17:00")
)

private data class CombinedSettings(
    val config: ENConfig,
    val timeFormatMode: TimeFormatMode,
    val dateFormatMode: DateFormatMode,
    val resolvedFormat: ResolvedDateTimeFormat
)

class ParserSettingsViewModel(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private var baselineConfig: ENConfig? = null

    private val _uiState = MutableStateFlow(ParserSettingsUiState())
    val uiState: StateFlow<ParserSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferenceRepository.getKwhenConfig(),
                preferenceRepository.getTimeFormatMode(),
                preferenceRepository.getDateFormatMode(),
                preferenceRepository.getResolvedDateTimeFormat()
            ) { config, timeFormatMode, dateFormatMode, resolvedFormat ->
                CombinedSettings(config, timeFormatMode, dateFormatMode, resolvedFormat)
            }.collect { settings ->
                val baseline = baselineConfig ?: settings.config.also { baselineConfig = it }
                val diffs = computeComparisonDiffs(baseline, settings.config)

                _uiState.update { state ->
                    state.copy(
                        morningHour = settings.config.morning,
                        afternoonHour = settings.config.afternoon,
                        eveningHour = settings.config.evening,
                        nightHour = settings.config.night,
                        use24 = settings.resolvedFormat.use24Hour,
                        useEuropeanDate = settings.resolvedFormat.useEuropeanDate,
                        timeFormatMode = settings.timeFormatMode,
                        dateFormatMode = settings.dateFormatMode,
                        is24HourResolved = settings.resolvedFormat.use24Hour,
                        isEuropeanDateResolved = settings.resolvedFormat.useEuropeanDate,
                        comparisonDiffs = diffs
                    )
                }
            }
        }
    }

    fun setMorningHour(hour: Int) {
        viewModelScope.launch {
            preferenceRepository.setMorningHour(hour)
        }
    }

    fun setAfternoonHour(hour: Int) {
        viewModelScope.launch {
            preferenceRepository.setAfternoonHour(hour)
        }
    }

    fun setEveningHour(hour: Int) {
        viewModelScope.launch {
            preferenceRepository.setEveningHour(hour)
        }
    }

    fun setNightHour(hour: Int) {
        viewModelScope.launch {
            preferenceRepository.setNightHour(hour)
        }
    }

    fun setUse24(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setUse24Hour(enabled)
        }
    }

    fun setUseEuropeanDate(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setUseEuropeanDate(enabled)
        }
    }

    fun setTimeFormatMode(mode: TimeFormatMode) {
        viewModelScope.launch {
            preferenceRepository.setTimeFormatMode(mode)
        }
    }

    fun setDateFormatMode(mode: DateFormatMode) {
        viewModelScope.launch {
            preferenceRepository.setDateFormatMode(mode)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            preferenceRepository.resetKwhenConfigToDefaults()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun computeComparisonDiffs(
        baselineConfig: ENConfig,
        currentConfig: ENConfig
    ): List<ComparisonDiffItem> {
        val now = Clock.System.now().toEpochMilliseconds()
        val tz = TimeZone.currentSystemDefault()
        val baselineParser = TimeParser(baselineConfig.copy(relativeTo = now, timeZone = tz))
        val currentParser = TimeParser(currentConfig.copy(relativeTo = now, timeZone = tz))

        return PresetPhrases.map { preset ->
            val prev = formatPhrase(preset.input, baselineParser, baselineConfig)
            val curr = formatPhrase(preset.input, currentParser, currentConfig)
            ComparisonDiffItem(
                label = preset.label,
                input = preset.input,
                previousOutput = prev,
                currentOutput = curr,
                hasChanged = prev != curr
            )
        }
    }

    private fun formatPhrase(
        input: String,
        timeParser: TimeParser,
        config: ENConfig
    ): String {
        val results = timeParser.parse(input)
        val first = results.firstOrNull() ?: return "No date detected"
        val dt = first.startTime.firstOrNull() ?: return first.text

        val timeStr = formatTime(dt.hour, dt.minute, config.use24)
        val isRecurring = first.repeatTag != null || input.contains("every", ignoreCase = true)
        val isTomorrow = input.contains("tomorrow", ignoreCase = true)
        val monthName = getShortMonthName(dt.month)

        return when {
            isRecurring -> {
                val dayName = dt.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                "Every $dayName, $timeStr"
            }
            isTomorrow -> "Tomorrow, $timeStr"
            config.useEuropeanDate -> "${dt.day} $monthName, ${dt.year}, $timeStr"
            else -> "$monthName ${dt.day}, ${dt.year}, $timeStr"
        }
    }

    private fun formatTime(hour: Int, minute: Int, use24: Boolean): String {
        val mStr = minute.toString().padStart(PadLength, '0')
        return if (use24) {
            val hStr = hour.toString().padStart(PadLength, '0')
            "$hStr:$mStr"
        } else {
            val period = if (hour < NoonHour) "AM" else "PM"
            val h12 = when (val h = hour % NoonHour) {
                0 -> NoonHour
                else -> h
            }
            "$h12:$mStr $period"
        }
    }

    private fun getShortMonthName(month: Month): String = when (month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
    }

    @Immutable
    data class ParserSettingsUiState(
        val morningHour: Int = 9,
        val afternoonHour: Int = 15,
        val eveningHour: Int = 18,
        val nightHour: Int = 20,
        val use24: Boolean = false,
        val useEuropeanDate: Boolean = false,
        val timeFormatMode: TimeFormatMode = TimeFormatMode.SYSTEM,
        val dateFormatMode: DateFormatMode = DateFormatMode.SYSTEM,
        val is24HourResolved: Boolean = false,
        val isEuropeanDateResolved: Boolean = false,
        val comparisonDiffs: List<ComparisonDiffItem> = emptyList()
    )
}
