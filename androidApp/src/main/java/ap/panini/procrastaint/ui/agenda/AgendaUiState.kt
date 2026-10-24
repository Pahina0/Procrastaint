package ap.panini.procrastaint.ui.agenda

import androidx.compose.runtime.Immutable
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.entities.TaskTag
import ap.panini.procrastaint.util.Date

@Immutable
data class AgendaUiState(
    val focusedDate: Long = Date.getTodayStart(),
    val isMonthExpanded: Boolean = false,
    val overdueTasks: List<TaskSingle> = emptyList(),
    val scheduledTasksByDate: Map<Long, List<TaskSingle>> = emptyMap(),
    val externalEventsByDate: Map<Long, List<ExternalCalendarEvent>> = emptyMap(),
    val monthTasksByDate: Map<Long, List<TaskSingle>> = emptyMap(),
    val tags: List<TaskTag> = emptyList(),
    val selectedTagId: Long? = null,
    val searchQuery: String = "",
    val hasDeviceCalendarPermission: Boolean = false,
    val isGoogleLoggedIn: Boolean = false
)
