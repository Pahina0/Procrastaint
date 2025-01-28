package ap.panini.procrastaint.data.entities

import ap.panini.procrastaint.util.Time

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

    val allDay: Boolean,

    val currentEventTime: Long, // make sure not -1 when accessing
)
