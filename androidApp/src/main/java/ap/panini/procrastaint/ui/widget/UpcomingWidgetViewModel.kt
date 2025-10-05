package ap.panini.procrastaint.ui.widget

import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.ui.viewmodel.CheckableTaskViewModel
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UpcomingWidgetViewModel(
    taskRepository: TaskRepository
) : CheckableTaskViewModel(taskRepository) {

    val recentlyCompleted: StateFlow<Set<Pair<Long, Long>>> = _recentlyCompleted

    val upcomingTasks: StateFlow<List<TaskSingle>> =
        taskRepository.getTasks(
            from = Date.getTodayStart(),
            to = Date.getTodayStart() + 7 * 24 * 60 * 60 * 1000L
        )
            .combine(_recentlyCompleted) { tasks, completed ->
                tasks.filter { task ->
                    task.completed == null || Pair(task.taskId, task.currentEventTime) in completed
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun toggleTaskCompletion(taskId: Long, completed: Boolean, completionId: Long) {
        viewModelScope.launch {
            val task = upcomingTasks.value.find { it.taskId == taskId } ?: return@launch
            checkTask(task)
        }
    }
}
