package ap.panini.procrastaint.data.repositories.calendars

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.entities.TaskInfo
import ap.panini.procrastaint.data.entities.TaskMeta
import kotlinx.coroutines.flow.Flow

sealed interface CalendarRepository {

    sealed interface Response {
        data object Success : Response
        class Error(ex: List<Throwable>) : Response {
            constructor(ex: Throwable) : this(listOf(ex))
        }
    }

    fun isLoggedIn(): Flow<Boolean>

    /**
     * Logout of the account
     *
     */
    suspend fun logout()

    /**
     * Login and create a calendar
     *
     * @param accessToken
     * @param refreshToken
     */
    suspend fun login(accessToken: String?, refreshToken: String?)

    suspend fun createCalendar(): Response

    suspend fun createTask(
        task: Task,
    ): Response

    suspend fun deleteTask(
        task: Task
    ): Response

    suspend fun addCompletion(
        task: Task,
        completion: TaskCompletion,
    ): Response

    suspend fun removeCompletion(
        task: Task,
        completion: TaskCompletion,
    ): Response

    suspend fun updateTask(
        task: Task,
    ): Response
}
