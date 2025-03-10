package ap.panini.procrastaint.data.network.api

import ap.panini.procrastaint.data.entities.google.GoogleCalendar
import ap.panini.procrastaint.data.entities.google.GoogleCalendarList
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

interface GoogleCalendarApi {

    companion object {
        const val BASE_URL = "https://www.googleapis.com/calendar/v3/"
    }

    @POST("calendars")
    suspend fun createCalendar(
        @Body calendar: GoogleCalendar,
    ): GoogleCalendar

    @GET("users/me/calendarList")
    suspend fun getCalendars(): GoogleCalendarList
}
