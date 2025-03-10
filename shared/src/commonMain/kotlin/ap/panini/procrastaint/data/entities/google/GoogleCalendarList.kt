package ap.panini.procrastaint.data.entities.google

import kotlinx.serialization.Serializable

@Serializable
data class GoogleCalendarList(
    val items: List<GoogleCalendar>
)
