package ap.panini.procrastaint.ui.widget

import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.ui.viewmodel.CheckableTaskViewModel
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UpcomingWidgetViewModel(
    taskRepository: TaskRepository
) : CheckableTaskViewModel(taskRepository) {

    val recentlyCompleted: StateFlow<Set<Pair<Long, Long>>> = recentlyCompletedInternal

    private val todayStartFlow = MutableStateFlow(Date.getTodayStart())

    val upcomingTasks: StateFlow<List<TaskSingle>> =
        todayStartFlow.flatMapLatest { todayStart ->
            taskRepository.getTasks(
                from = todayStart,
                to = todayStart + 7 * 24 * 60 * 60 * 1000L
            )
        }
            .combine(recentlyCompletedInternal) { tasks, completed ->
                tasks.filter { task ->
                    task.completed == null || Pair(task.taskId, task.currentEventTime) in completed
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun refresh() {
        todayStartFlow.value = Date.getTodayStart()
    }

    fun toggleTaskCompletion(taskId: Long, eventTime: Long? = null) {
        viewModelScope.launch {
            refresh()
            val task = if (eventTime != null) {
                upcomingTasks.value.find { it.taskId == taskId && it.currentEventTime == eventTime }
                    ?: upcomingTasks.value.find { it.taskId == taskId }
            } else {
                upcomingTasks.value.find { it.taskId == taskId }
            } ?: return@launch
            checkTask(task)
        }
    }
}
