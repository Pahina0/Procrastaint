package ap.panini.procrastaint.ui.calendar

import ap.panini.procrastaint.data.entities.TaskSingle
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

data class CalendarPageData(
    val time: Long,
    val tasksByDay: Flow<Map<LocalDate, List<TaskSingle>>>
)