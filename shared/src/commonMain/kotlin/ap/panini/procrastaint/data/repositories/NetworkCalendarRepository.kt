package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.entities.Task
import ap.panini.procrastaint.data.entities.TaskCompletion
import ap.panini.procrastaint.data.repositories.calendars.GoogleCalendarRepository

/**
 * Calendar repository used to access network api's of calendars
 * also used for syncing
 *
 * @constructor Create empty Calendar repository
 */
class NetworkCalendarRepository(
    private val googleCalendarRepository: GoogleCalendarRepository
) {

    /**
     * Google create calendar
     * creates a calendar for the application. if one already exists then it saves that one
     * this is called only once when logging into google
     */
    suspend fun googleCreateCalendar() {
        googleCalendarRepository.createCalendar()
    }

    suspend fun createEvent(task: Task) {
        googleCalendarRepository.createEvent(task, onFailure = {println(it)})
    }

    suspend fun addCompletion(task: Task, completion: TaskCompletion) {
        googleCalendarRepository.addCompletion(task, completion)
    }
}
