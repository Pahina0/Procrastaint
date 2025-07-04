package ap.panini.procrastaint.ui.upcoming

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpcomingViewModel(
    private val db: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpcomingUiState())
    val uiState: StateFlow<UpcomingUiState> = _uiState.asStateFlow()

    init {
        getAllTasks()
    }

    private fun getAllTasks() {
        viewModelScope.launch {
            db.getTasks(
                Date.getTodayStart(),
                maxRepetition = 6
            ).flowOn(Dispatchers.IO).collectLatest { taskInfos: List<TaskSingle> ->
                _uiState.update {
                    it.copy(
                        taskInfos = taskInfos.filter { f -> f.completed == null }
                    )
                }
            }
        }
    }

    fun checkTask(task: TaskSingle) {
        viewModelScope.launch {
            val completion =
                TaskCompletion(
                    Date.getTime(),
                    task.currentEventTime,
                    task.taskId,
                    task.metaId,
                    task.completionId
                )
            if (task.completed == null) {
                db.addCompletion(
                    completion
                )
            } else {
                db.removeCompletion(
                    completion
                )
            }
        }
    }

    @Immutable
    data class UpcomingUiState(
        val taskInfos: List<TaskSingle> = listOf(),
    )
}
