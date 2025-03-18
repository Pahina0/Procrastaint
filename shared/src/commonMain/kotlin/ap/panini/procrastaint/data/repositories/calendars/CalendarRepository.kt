package ap.panini.procrastaint.data.repositories.calendars

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion

interface CalendarRepository {

    sealed interface Response {
        data object Success : Response
        class Error(ex: List<Throwable>) : Response {
            constructor(ex: Throwable) : this(listOf(ex))
        }
    }

    suspend fun createCalendar(
    ): Response

    suspend fun createEvent(
        task: Task,
    ): Response

    suspend fun addCompletion(
        task: Task,
        completion: TaskCompletion,
    ): Response

    suspend fun removeCompletion(
        task: Task,
        completion: TaskCompletion,
    ): Response
}
