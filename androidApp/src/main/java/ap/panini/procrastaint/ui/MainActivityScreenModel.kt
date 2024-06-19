package ap.panini.procrastaint.ui

import androidx.compose.runtime.Immutable
import ap.panini.procrastaint.data.AppRepository
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import ap.panini.procrastaint.util.TaskGroup
import ap.panini.procrastaint.util.Time
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivityScreenModel(
    private val db: AppRepository
) : StateScreenModel<MainActivityScreenModel.State>(State()) {
    private var parser = Parser()

    @Immutable
    data class State(
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
        screenModelScope.launch {
            db.insertTask(
                state.value.run {
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

            mutableState.update { State() }
        }
    }

    fun setRepeatTag(tag: Time?) {
        mutableState.update {
            it.copy(
                repeatTag = tag
            )
        }
    }

    fun setRepeatInterval(interval: Int?) {
        mutableState.update {
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
        mutableState.update {
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
        mutableState.update {
            it.copy(
                manualStartTimes = if (state.value.manualStartTimes.contains(time)) {
                    it.manualStartTimes - time
                } else {
                    it.manualStartTimes + time
                }
            )
        }
    }

    fun editEndTime(time: Long?) {
        mutableState.update {
            it.copy(
                endTime = time
            )
        }
    }

    fun viewNextParsed() {
        if (state.value.viewing == -1) return

        mutableState.update {
            it.copy(
                viewing = (it.viewing + 1) % (it.autoParsed.size + 1)
            )
        }
    }

    fun updateDescription(description: String) {
        mutableState.update {
            it.copy(
                description = description
            )
        }
    }
}
