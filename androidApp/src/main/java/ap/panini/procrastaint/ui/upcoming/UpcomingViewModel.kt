package ap.panini.procrastaint.ui.upcoming

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.ui.viewmodel.CheckableTaskViewModel
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpcomingViewModel(
    db: TaskRepository,
) : CheckableTaskViewModel(db) {

    private val _uiState = MutableStateFlow(UpcomingUiState())
    val uiState: StateFlow<UpcomingUiState> = _uiState.asStateFlow()

    val recentlyCompleted: StateFlow<Set<Pair<Long, Long>>> = _recentlyCompleted

    init {
        getAllTasks()
    }

    private fun getAllTasks() {
        viewModelScope.launch {
            taskRepository.getTasks(
                Date.getTodayStart(),
                maxRepetition = 6,
                includeNoTimeTasks = true
            ).flowOn(Dispatchers.IO).combine(_recentlyCompleted) { taskInfos, recentlyCompleted ->
                taskInfos.filter { f ->
                    f.completed == null || Pair(
                        f.taskId,
                        f.currentEventTime
                    ) in recentlyCompleted
                }
            }.collectLatest { taskInfos: List<TaskSingle> ->
                _uiState.update {
                    it.copy(
                        taskInfos = taskInfos.filter { f -> f.completed == null }
                    )
                }
            }
        }
    }

    @Immutable
    data class UpcomingUiState(
        val taskInfos: List<TaskSingle> = listOf(),
    )
}
