package ap.panini.procrastaint.data.entities.google

import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleCalendar(
    val summary: String,
    val id: String? = null
)
