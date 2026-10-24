package ap.panini.procrastaint.ui.settings.parser

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParserSettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val parser: Parser
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParserSettingsUiState())
    val uiState: StateFlow<ParserSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceRepository.getKwhenConfig().collect { config ->
                _uiState.update { state ->
                    state.copy(
                        morningHour = config.morning,
                        afternoonHour = config.afternoon,
                        eveningHour = config.evening,
                        nightHour = config.night,
                        use24 = config.use24,
                        useEuropeanDate = config.useEuropeanDate,
                        sandboxParsed = parser.parse(state.sandboxInput)
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

    fun resetToDefaults() {
        viewModelScope.launch {
            preferenceRepository.resetKwhenConfigToDefaults()
        }
    }

    fun updateSandboxInput(input: String) {
        _uiState.update { state ->
            state.copy(
                sandboxInput = input,
                sandboxParsed = parser.parse(input)
            )
        }
    }

    @Immutable
    data class ParserSettingsUiState(
        val morningHour: Int = 9,
        val afternoonHour: Int = 15,
        val eveningHour: Int = 18,
        val nightHour: Int = 20,
        val use24: Boolean = false,
        val useEuropeanDate: Boolean = false,
        val sandboxInput: String = "Doctor appointment tomorrow evening #health",
        val sandboxParsed: Parsed? = null
    )
}
