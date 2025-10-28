package ap.panini.procrastaint.ui.calendar

import ap.panini.procrastaint.data.entities.TaskSingle
import kotlinx.coroutines.flow.Flow

data class CalendarPageData(val time: Long, val tasks: Flow<List<TaskSingle>>)
