package ap.panini.procrastaint.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

private const val UndoDelay = 1500L

open class CheckableTaskViewModel(
    protected val taskRepository: TaskRepository
) : ViewModel() {

    protected val recentlyCompletedInternal = MutableStateFlow<Set<Pair<Long, Long>>>(setOf())
    private val pendingCompletions = ConcurrentHashMap<Pair<Long, Long>, Job>()

    open fun checkTask(task: TaskSingle) {
        val id = Pair(task.taskId, task.currentEventTime)
        val pendingJob = pendingCompletions.remove(id)

        if (pendingJob != null) {
            // Task was checked and is pending DB write: immediately cancel and uncheck
            pendingJob.cancel()
            recentlyCompletedInternal.update { it - id }
            return
        }

        if (task.completed != null) {
            // Immediately un-complete in database
            recentlyCompletedInternal.update { it - id }
            viewModelScope.launch {
                taskRepository.removeCompletion(
                    TaskCompletion(
                        completionId = task.completionId,
                        taskId = task.taskId,
                        completionTime = Date.getTime(),
                        forTime = task.currentEventTime,
                        metaId = task.metaId
                    )
                )
            }
        } else {
            // Add to recently completed optimistically, delay, then write to database
            recentlyCompletedInternal.update { it + id }
            val job = viewModelScope.launch {
                try {
                    delay(UndoDelay)
                    taskRepository.addCompletion(
                        TaskCompletion(
                            completionTime = Date.getTime(),
                            forTime = task.currentEventTime,
                            taskId = task.taskId,
                            metaId = task.metaId
                        )
                    )
                } finally {
                    pendingCompletions.remove(id)
                    recentlyCompletedInternal.update { it - id }
                }
            }
            pendingCompletions[id] = job
        }
    }
}
