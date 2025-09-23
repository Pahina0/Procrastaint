package ap.panini.procrastaint.ui.upcoming

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpcomingViewModel(
    private val db: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpcomingUiState())
    val uiState: StateFlow<UpcomingUiState> = _uiState.asStateFlow()

    private val _recentlyCompleted = MutableStateFlow<Set<Long>>(setOf())
    val recentlyCompleted: StateFlow<Set<Long>> = _recentlyCompleted

    init {
        getAllTasks()
    }

    private fun getAllTasks() {
        viewModelScope.launch {
            db.getTasks(
                Date.getTodayStart(),
                maxRepetition = 6,
                includeNoTimeTasks = true
            ).flowOn(Dispatchers.IO).combine(_recentlyCompleted) { taskInfos, recentlyCompleted ->
                taskInfos.filter { f -> f.completed == null || f.taskId in recentlyCompleted }
            }.collectLatest { taskInfos: List<TaskSingle> ->
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
            if (task.completed != null) {
                // Immediately un-complete
                db.removeCompletion(
                    TaskCompletion(
                        completionId = task.completionId,
                        taskId = task.taskId,
                        completionTime = Date.getTime(),
                        forTime = task.currentEventTime,
                        metaId = task.metaId
                    )
                )
            } else {
                // Add to recently completed and then mark as complete after a delay
                _recentlyCompleted.value += task.taskId
                delay(1500)
                db.addCompletion(
                    TaskCompletion(
                        completionTime = Date.getTime(),
                        forTime = task.currentEventTime,
                        taskId = task.taskId,
                        metaId = task.metaId
                    )
                )
                _recentlyCompleted.value -= task.taskId
            }
        }
    }

    @Immutable
    data class UpcomingUiState(
        val taskInfos: List<TaskSingle> = listOf(),
    )
}
