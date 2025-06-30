package ap.panini.procrastaint.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivityViewModel(
    private val db: TaskRepository,
    private var parser: Parser
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    @Immutable
    data class MainUiState(
        val task: String = "",
        val description: String = "",
        val parsed: Parsed? = null,
        val viewing: Int = -1,
        val visible: Boolean = false,

        val mode: Mode = Mode.Create
    ) {
        sealed interface Mode {
            data object Create : Mode
            class Edit(val taskId: Long) : Mode
        }
    }

    fun getTagColor(tag: String): Color? {
        return runBlocking {
            db.getTagOrNull(tag)?.color?.let {
                TaskTag.hexToRgb(it).let {
                    Color(it.first, it.second, it.third)
                }
            }
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
                    task = task.generateOriginalText(),
                    description = task.taskInfo.description,
                    mode = MainUiState.Mode.Edit(taskId)
                )
            }
        }
    }

    fun deleteEditTask() {
        if (uiState.value.mode !is MainUiState.Mode.Edit) return

        viewModelScope.launch {
            val id = (uiState.value.mode as? MainUiState.Mode.Edit)?.taskId ?: return@launch
            db.deleteTask(id)
        }
        onHide()
    }

    fun onShow() {
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
            val task = with(uiState.value) {
                parsed?.toTask(
                    timeIndex = viewing,
                    description = description
                )
            }

            if (task == null) return@launch

            when (uiState.value.mode) {
                is MainUiState.Mode.Create -> {
                    val success = db.insertTask(task)

                    if (success) {
                        _uiState.update { MainUiState() }
                    }
                }

                is MainUiState.Mode.Edit -> {
                    val idCorrected = task.taskInfo.copy(
                        taskId = (uiState.value.mode as MainUiState.Mode.Edit).taskId
                    )

                    db.editTask(
                        task.copy(
                            taskInfo = idCorrected
                        )
                    )
                }
            }
        }

        onHide()
    }

//    fun setRepeatTag(tag: Time?) {
//        _uiState.update {
//            it.copy(
//                repeatTag = tag
//            )
//        }
//    }
//
//    fun setRepeatInterval(interval: Int?) {
//        _uiState.update {
//            it.copy(
//                repeatInterval = interval
//            )
//        }
//    }

    fun updateTask(title: String) {
        val parsed = parser.parse(title)
        _uiState.update {
            it.copy(
                task = title,
                parsed = parsed,
                viewing = if (parsed.times.isEmpty()) {
                    -1
                } else if (it.viewing == parsed.times.size) {
                    parsed.times.size
                } else if (it.viewing < parsed.times.size && it.viewing != -1) {
                    it.viewing
                } else {
                    0
                }
            )
        }
    }

    fun viewNextParsed() {
        if (uiState.value.viewing == -1) return

        _uiState.update {
            it.copy(
                viewing = (it.viewing + 1) % (it.parsed?.times?.size?.plus(1) ?: 0)
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
