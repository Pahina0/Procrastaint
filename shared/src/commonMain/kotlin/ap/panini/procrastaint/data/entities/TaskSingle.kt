package ap.panini.procrastaint.data.entities

import ap.panini.procrastaint.util.Time

/**
 * flattened task for displaying
 */
data class TaskSingle(
    val taskId: Long,
    val metaId: Long,

    val title: String,
    val description: String,
    val completed: Long?,

    val currentEventTime: Long,

    val startTime: Long?,
    val endTime: Long?,
    val repeatTag: Time?,
    val repeatOften: Int?,

    val allDay: Boolean,
)
