package ap.panini.procrastaint.data.repositories.calendars

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion

interface CalendarRepository {
    suspend fun createCalendar(
        onSuccess: () -> Unit = {},
        onFailure: (ex: Throwable) -> Unit = {}
    )

    suspend fun createEvent(
        task: Task,
        onSuccess: () -> Unit = {},
        onFailure: (ex: Throwable) -> Unit = {}
    )

    suspend fun addCompletion(
        task: Task,
        completion: TaskCompletion,
        onSuccess: () -> Unit = {},
        onFailure: (ex: Throwable) -> Unit = {}
    )

    suspend fun removeCompletion(
        task: Task,
        completion: TaskCompletion,
        onSuccess: () -> Unit = {},
        onFailure: (ex: Throwable) -> Unit = {}
    )
}
