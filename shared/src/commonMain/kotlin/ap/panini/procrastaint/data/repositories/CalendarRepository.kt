package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.entities.google.GoogleCalendar
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi

class CalendarRepository(
    private val preference: PreferenceRepository, private val gcApi: GoogleCalendarApi
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
}
