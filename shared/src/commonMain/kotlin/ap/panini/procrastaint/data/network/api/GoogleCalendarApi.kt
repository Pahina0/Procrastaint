package ap.panini.procrastaint.data.network.api

import ap.panini.procrastaint.data.entities.google.GoogleCalendar
import ap.panini.procrastaint.data.entities.google.GoogleCalendarList
import ap.panini.procrastaint.data.entities.google.GoogleEvent
import ap.panini.procrastaint.data.entities.google.GoogleEventInstances
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.flow.Flow

internal interface GoogleCalendarApi {

    companion object {
        const val BASE_URL = "https://www.googleapis.com/calendar/v3/"
    }

    @POST("calendars")
    fun createCalendar(
        @Body calendar: GoogleCalendar,
    ): Flow<GoogleCalendar>

    @GET("users/me/calendarList")
    fun getCalendars(): Flow<GoogleCalendarList>

    @POST("calendars/{calendarId}/events")
    fun createEvent(
        @Body event: GoogleEvent,
        @Path calendarId: String,
    ): Flow<GoogleEvent>

    @DELETE("calendars/{calendarId}/events/{eventId}")
    fun deleteEvent(
        @Path calendarId: String,
        @Path eventId: String
    ): Flow<String>

    @PUT("calendars/{calendarId}/events/{eventId}")
    fun updateEvent(
        @Body event: GoogleEvent,
        @Path calendarId: String,
        @Path eventId: String
    ): Flow<GoogleEvent>

    @GET("calendars/{calendarId}/events/{eventId}/instances")
    fun getInstances(
        @Path calendarId: String,
        @Path eventId: String,
        @Query timeMax: String, // exclusive
        @Query timeMin: String // inclusive
    ): Flow<GoogleEventInstances>
}
