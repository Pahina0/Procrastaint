package ap.panini.procrastaint.data.entities

/**
 * Common polymorphic interface for agenda and calendar timeline items
 */
sealed interface ScheduleItem {
    val id: String
    val title: String
    val startTime: Long?
    val endTime: Long?
    val isAllDay: Boolean
    val color: Int?
    val sourceName: String

    data class TaskOccurrence(
        val task: TaskSingle
    ) : ScheduleItem {
        override val id: String get() = "task_${task.taskId}_${task.currentEventTime}"
        override val title: String get() = task.title
        override val startTime: Long? get() = task.startTime
        override val endTime: Long? get() = task.endTime
        override val isAllDay: Boolean get() = task.startTime == null
        override val color: Int? get() = task.tags.firstOrNull()?.toRgbOrNull()?.let {
            (0xFF shl 24) or (it.first shl 16) or (it.second shl 8) or it.third
        }
        override val sourceName: String get() = task.tags.firstOrNull()?.displayTitle?.ifBlank {
            task.tags.first().title
        } ?: "Procrastaint"
    }

    data class ExternalCalendar(
        val event: ExternalCalendarEvent
    ) : ScheduleItem {
        override val id: String get() = "external_${event.id}_${event.startTime}"
        override val title: String get() = event.title
        override val startTime: Long get() = event.startTime
        override val endTime: Long get() = event.endTime
        override val isAllDay: Boolean get() = event.isAllDay
        override val color: Int? get() = event.color
        override val sourceName: String
            get() = if (event.source == CalendarSource.GOOGLE) {
                "Google Calendar"
            } else {
                "Device Calendar"
            }
    }
}
