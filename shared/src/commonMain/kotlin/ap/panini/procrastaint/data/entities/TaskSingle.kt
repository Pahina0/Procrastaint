package ap.panini.procrastaint.data.entities

import androidx.room.Ignore
import ap.panini.procrastaint.util.Time
import kotlinx.serialization.Serializable

/**
 * flattened task for displaying
 */
@Serializable
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

    @Ignore
    val tags: List<TaskTag> = emptyList()
) {
    constructor(
        taskId: Long,
        metaId: Long,
        completionId: Long,
        title: String,
        description: String,
        completed: Long?,
        startTime: Long?,
        endTime: Long?,
        repeatTag: Time?,
        repeatOften: Int?,
        currentEventTime: Long
    ) : this(
        taskId,
        metaId,
        completionId,
        title,
        description,
        completed,
        startTime,
        endTime,
        repeatTag,
        repeatOften,
        currentEventTime,
        emptyList()
    )
}
