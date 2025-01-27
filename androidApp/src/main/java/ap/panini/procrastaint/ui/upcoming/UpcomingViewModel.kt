package ap.panini.procrastaint.ui.upcoming

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.AppRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

class UpcomingViewModel(
    private val db: AppRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpcomingUiState())
    val uiState: StateFlow<UpcomingUiState> = _uiState.asStateFlow()

    init {
        getAllTasks()
    }

    private fun getAllTasks() {
        viewModelScope.launch {
            db.getUpcomingTasks(
                Date.getTodayStart(), Date.getTodayStart() + 7.days.inWholeMilliseconds
            ).flowOn(Dispatchers.IO).collectLatest { taskInfos: List<TaskSingle> ->
                    _uiState.update {
                        println(taskInfos)
                        it.copy(
                            taskInfos = taskInfos
                        )
                    }
                }
        }
    }

    fun checkTask(task: TaskSingle) {
        viewModelScope.launch {
            if (task.completed == null) {
                db.addCompletion(
                    TaskCompletion(Date.getTime(), task.currentEventTime, task.taskId, task.metaId, task.completionId)
                )
            } else {
                db.removeCompletion(
                    TaskCompletion(Date.getTime(), task.currentEventTime, task.taskId, task.metaId, task.completionId)
                )
            }
        }
    }

    @Immutable
    data class UpcomingUiState(
        val taskInfos: List<TaskSingle> = listOf(),
    )
}
