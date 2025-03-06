package ap.panini.procrastaint.data.repositories

import ap.panini.procrastaint.data.network.api.GoogleCalendarApi

class CalendarRepository(
    private val gcApi: GoogleCalendarApi
) {

    suspend fun createCalendar() = gcApi.createCalendar("Procrastaint")
}