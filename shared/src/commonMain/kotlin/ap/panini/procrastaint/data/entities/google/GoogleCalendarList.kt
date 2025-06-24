package ap.panini.procrastaint.data.entities.google

import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleCalendarList(
    val items: List<GoogleCalendar>
)
