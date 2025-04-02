package ap.panini.procrastaint.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.repositories.TaskRepository
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
    private val db: TaskRepository
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
        val visible: Boolean = false,

        val mode: Mode = Mode.Create
    ) {
        sealed interface Mode {
            data object Create : Mode
            class Edit(val task: Task) : Mode
        }
    }

    /**
     * Edit created task launches the bottom sheet and puts you in the state to edit a task
     *
     * @param taskId
     */
    fun editCreatedTask(taskId: Long) {
        viewModelScope.launch {
            onShow()
            val task = db.getTask(taskId)

            _uiState.update {
                uiState.value.copy(
                    task = task.taskInfo.title,
                    description = task.taskInfo.description,
                    manualStartTimes = task.meta.mapNotNull { it.startTime }.toSet(),
                    endTime = task.meta.mapNotNull { it.endTime }.maxOrNull(),
                    repeatInterval = task.meta.firstOrNull()?.repeatOften,
                    repeatTag = task.meta.firstOrNull()?.repeatTag,
                    mode = MainUiState.Mode.Edit(task)
                )
            }
        }
    }

    fun deleteEditTask() {
        if (uiState.value.mode !is MainUiState.Mode.Edit) return

        viewModelScope.launch {
            db.deleteTask((uiState.value.mode as MainUiState.Mode.Edit).task)
        }
        onHide()
    }

    fun onShow() {
        if (_uiState.value.task.isBlank()) {
            parser = Parser() // resets the time to now
        }

        _uiState.update { uiState.value.copy(visible = true) }
    }

    fun onHide() {
        _uiState.update {
            if (_uiState.value.mode is MainUiState.Mode.Edit) {
                MainUiState()
            } else {
                uiState.value.copy(visible = false)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val task =
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
                }.toTask()

            if (task == null) return@launch

            when (uiState.value.mode) {
                is MainUiState.Mode.Create -> {
                    val success = db.insertTask(task)

                    if (success) {
                        _uiState.update { MainUiState() }
                    }
                }

                is MainUiState.Mode.Edit -> {
                    db.editTask((uiState.value.mode as MainUiState.Mode.Edit).task, task)
                }
            }
        }

        onHide()
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
