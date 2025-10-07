package ap.panini.procrastaint.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

open class CheckableTaskViewModel(
    protected val taskRepository: TaskRepository
) : ViewModel() {

    protected val _recentlyCompleted = MutableStateFlow<Set<Pair<Long, Long>>>(setOf())

    fun checkTask(task: TaskSingle) {
        viewModelScope.launch {
            val id = Pair(task.taskId, task.currentEventTime)
            if (task.completed != null) {
                // Immediately un-complete
                taskRepository.removeCompletion(
                    TaskCompletion(
                        completionId = task.completionId,
                        taskId = task.taskId,
                        completionTime = Date.getTime(),
                        forTime = task.currentEventTime,
                        metaId = task.metaId
                    )
                )
                _recentlyCompleted.value -= id
            } else {
                // Add to recently completed and then mark as complete after a delay
                _recentlyCompleted.value += id
                delay(1500)
                taskRepository.addCompletion(
                    TaskCompletion(
                        completionTime = Date.getTime(),
                        forTime = task.currentEventTime,
                        taskId = task.taskId,
                        metaId = task.metaId
                    )
                )
                _recentlyCompleted.value -= id
            }
        }
    }
}
