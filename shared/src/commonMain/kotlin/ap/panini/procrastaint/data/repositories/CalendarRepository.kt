package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.entities.GoogleCalendar
import ap.panini.procrastaint.data.network.api.GoogleCalendarApi

class CalendarRepository(
    private val preference: PreferenceRepository,
    private val gcApi: GoogleCalendarApi
) {

    suspend fun googleCreateCalendar() {
        preference.setString(
            PreferenceRepository.GOOGLE_CALENDAR_ID,
            gcApi.createCalendar(GoogleCalendar("Procrastaint")).id!!
        )
    }

}