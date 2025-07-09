package ap.panini.procrastaint.data.entities

import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.Time
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * flattened task for displaying
 */
data class TaskSingle(
    val taskId: Long,
    val metaId: Long,
    val completionId: Long,

    val title: String,
    val description: String,
    val completed: Long?,

    val startTime: Long?,
    val endTime: Long?,
    val repeatTag: Time?,
    val repeatOften: Int?,

    val currentEventTime: Long, // make sure not -1 when accessing

) : KoinComponent {
    private val db: TaskRepository by inject()

    val tags: List<TaskTag>
        get() = runBlocking { db.getTask(taskId).tags }
}
