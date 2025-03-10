package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.entities.google.GoogleCalendar
import ap.panini.procrastaint.data.entities.google.GoogleEvent
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi
import kotlinx.coroutines.flow.first

/**
 * Calendar repository used to access network api's of calendars
 *
 * @property preference
 * @property gcApi
 * @constructor Create empty Calendar repository
 */
class CalendarRepository(
    private val preference: PreferenceRepository,
    private val gcApi: GoogleCalendarApi
) {

    suspend fun googleCreateCalendar() {
        val existingCalendars = gcApi.getCalendars()

        // creates a new calendar if only one doesn't exist already
        val possibleCalendar = existingCalendars.items.filter { it.summary == "Procrastaint" }

        preference.setString(
            PreferenceRepository.GOOGLE_CALENDAR_ID,
            possibleCalendar.firstOrNull()?.id
                ?: gcApi.createCalendar(GoogleCalendar("Procrastaint")).id!!
        )
    }

    suspend fun googleCreateEvent(event: GoogleEvent) = gcApi.createEvent(
        event,
        preference.getString(PreferenceRepository.GOOGLE_CALENDAR_ID).first()
    )
}
