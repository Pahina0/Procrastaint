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

private const val SubscriptionStopTimeoutMillis = 5000L
private const val UpcomingDaysLimit = 7L
private const val MillisecondsInDay = 24 * 60 * 60 * 1000L

@OptIn(ExperimentalCoroutinesApi::class)
class UpcomingWidgetViewModel(
    taskRepository: TaskRepository
) : CheckableTaskViewModel(taskRepository) {

    val recentlyCompleted: StateFlow<Set<Pair<Long, Long>>> = recentlyCompletedInternal

    private val todayStartFlow = MutableStateFlow(Date.getTodayStart())

    val overdueTasks: StateFlow<List<TaskSingle>> =
        todayStartFlow.flatMapLatest { todayStart ->
            taskRepository.getOverdueTasks(todayStart)
        }
            .combine(recentlyCompletedInternal) { tasks, completed ->
                tasks.filter { task ->
                    task.completed == null || Pair(task.taskId, task.currentEventTime) in completed
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SubscriptionStopTimeoutMillis),
                initialValue = emptyList()
            )

    val upcomingTasks: StateFlow<List<TaskSingle>> =
        todayStartFlow.flatMapLatest { todayStart ->
            taskRepository.getTasks(
                from = todayStart,
                to = todayStart + UpcomingDaysLimit * MillisecondsInDay
            )
        }
            .combine(recentlyCompletedInternal) { tasks, completed ->
                tasks.filter { task ->
                    task.completed == null || Pair(task.taskId, task.currentEventTime) in completed
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SubscriptionStopTimeoutMillis),
                initialValue = emptyList()
            )

    fun refresh() {
        todayStartFlow.value = Date.getTodayStart()
    }

    fun toggleTaskCompletion(taskId: Long, eventTime: Long? = null) {
        viewModelScope.launch {
            refresh()
            val task = findTask(overdueTasks.value, taskId, eventTime)
                ?: findTask(upcomingTasks.value, taskId, eventTime)
                ?: return@launch
            checkTask(task)
        }
    }

    private fun findTask(tasks: List<TaskSingle>, taskId: Long, eventTime: Long?): TaskSingle? {
        return if (eventTime != null) {
            tasks.find { it.taskId == taskId && it.currentEventTime == eventTime }
                ?: tasks.find { it.taskId == taskId }
        } else {
            tasks.find { it.taskId == taskId }
        }
    }
}
