package ap.panini.procrastaint.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.repositories.AppRepository
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import ap.panini.procrastaint.util.TaskGroup
import ap.panini.procrastaint.util.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val db: AppRepository
) : ViewModel() {
    private var parser = Parser()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    @Immutable
    data class MainUiState(
        val task: String = "",
        val description: String = "",
        val manualStartTimes: Set<Long> = emptySet(),
        val endTime: Long? = null,
        val repeatInterval: Int? = null,
        val repeatTag: Time? = null,
        val viewing: Int = -1,
        val autoParsed: List<Parsed> = emptyList(),
    )

    fun save() {
        viewModelScope.launch {
            val success = db.insertTask(
                uiState.value.run {
                    val curParsed = autoParsed.getOrNull(viewing)
                    TaskGroup(
                        startTimes = manualStartTimes + (curParsed?.startTimes ?: emptySet()),
                        endTime = endTime ?: curParsed?.endTime,
                        title = if (curParsed?.extractedRange == null) {
                            task
                        } else {
                            task.substring(0..<curParsed.extractedRange.first) + task.substring(
                                curParsed.extractedRange.last + 1
                            )
                        },
                        description = description,
                        repeatTag = repeatTag ?: curParsed?.repeatTag,
                        repeatOften = repeatInterval ?: curParsed?.repeatOften ?: 0
                    )
                }
            )

            if (success) {
                _uiState.update { MainUiState() }
            }
        }
    }

    fun setRepeatTag(tag: Time?) {
        _uiState.update {
            it.copy(
                repeatTag = tag
            )
        }
    }

    fun setRepeatInterval(interval: Int?) {
        _uiState.update {
            it.copy(
                repeatInterval = interval
            )
        }
    }

    fun updateTask(title: String) {
        if (title.isBlank()) {
            parser = Parser() // resets the time to now
        }
        val parsed = parser.parse(title)
        _uiState.update {
            it.copy(
                task = title,
                autoParsed = parsed,
                viewing = if (parsed.isEmpty()) {
                    -1
                } else if (it.viewing == it.autoParsed.size) {
                    parsed.size
                } else if (it.viewing < parsed.size && it.viewing != -1) {
                    it.viewing
                } else {
                    0
                }
            )
        }
    }

    fun editManualStartTime(time: Long) {
        _uiState.update {
            it.copy(
                manualStartTimes = if (uiState.value.manualStartTimes.contains(time)) {
                    it.manualStartTimes - time
                } else {
                    it.manualStartTimes + time
                }
            )
        }
    }

    fun editEndTime(time: Long?) {
        _uiState.update {
            it.copy(
                endTime = time
            )
        }
    }

    fun viewNextParsed() {
        if (uiState.value.viewing == -1) return

        _uiState.update {
            it.copy(
                viewing = (it.viewing + 1) % (it.autoParsed.size + 1)
            )
        }
    }

    fun updateDescription(description: String) {
        _uiState.update {
            it.copy(
                description = description
            )
        }
    }
}
