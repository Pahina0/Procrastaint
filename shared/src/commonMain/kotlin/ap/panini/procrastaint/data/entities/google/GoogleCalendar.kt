package ap.panini.procrastaint.data.entities.google

import kotlinx.serialization.Serializable

@Serializable
data class GoogleCalendar(
    val summary: String,
    val id: String? = null
)
