package ap.panini.procrastaint.ui.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddButtonWidgetViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _recentlyCompleted = MutableStateFlow<Set<Long>>(setOf())
    val recentlyCompleted: StateFlow<Set<Long>> = _recentlyCompleted

    val upcomingTasks: StateFlow<List<TaskSingle>> =
        taskRepository.getTasks(
            from = Date.getTodayStart(),
            to = Date.getTodayStart() + 7 * 24 * 60 * 60 * 1000L
        )
            .combine(_recentlyCompleted) { tasks, completed ->
                tasks.filter { it.completed == null || it.taskId in completed }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun toggleTaskCompletion(taskId: Long, completed: Boolean, completionId: Long) {
        viewModelScope.launch {
            if (completed) {
                // Immediately un-complete
                val task = upcomingTasks.value.find { it.taskId == taskId } ?: return@launch
                taskRepository.removeCompletion(
                    TaskCompletion(
                        completionTime = Date.getTime(),
                        forTime = task.currentEventTime,
                        taskId = task.taskId,
                        metaId = task.metaId,
                        completionId = completionId
                    )
                )
            } else {
                // Add to recently completed and then mark as complete after a delay
                _recentlyCompleted.value += taskId
                delay(1500)
                val task = upcomingTasks.value.find { it.taskId == taskId } ?: return@launch
                taskRepository.addCompletion(
                    TaskCompletion(
                        completionTime = Date.getTime(),
                        forTime = task.currentEventTime,
                        taskId = task.taskId,
                        metaId = task.metaId
                    )
                )
                _recentlyCompleted.value -= taskId
            }
        }
    }
}
