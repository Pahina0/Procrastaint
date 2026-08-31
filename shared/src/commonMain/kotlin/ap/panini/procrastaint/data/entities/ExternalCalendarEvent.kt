package ap.panini.procrastaint.data.entities

import kotlinx.serialization.Serializable

@Serializable
enum class CalendarSource {
    PROCRASTAINT,
    DEVICE,
    GOOGLE
}

@Serializable
data class ExternalCalendarEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean = false,
    val source: CalendarSource = CalendarSource.DEVICE,
    val color: Int? = null,
    val description: String? = null,
    val location: String? = null
)
