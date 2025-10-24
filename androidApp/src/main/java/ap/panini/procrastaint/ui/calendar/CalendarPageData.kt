package ap.panini.procrastaint.ui.calendar

import ap.panini.procrastaint.data.entities.TaskSingle
import kotlinx.coroutines.flow.Flow

sealed class CalendarPageData {
    abstract val time: Long

    data class Daily(override val time: Long, val tasks: Flow<Map<Int, List<TaskSingle>>>) : CalendarPageData()
    data class Monthly(override val time: Long, val tasks: Flow<List<TaskSingle>>) : CalendarPageData()
}
